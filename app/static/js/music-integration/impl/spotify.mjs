import { appStore } from "../../caffeinated.mjs";
import { MusicIntegration, MusicTrack, MusicStates } from "../music-util.mjs";

const AUTH_PROXY_URL = "https://api.casterlabs.co/v2/natsukashii/spotify";

class SpotifyIntegration extends MusicIntegration {
    spotifyProfile = null;

    constructor() {
        super("Spotify", "SPOTIFY");
    }

    isEnabled() {
        return appStore.get("music_integration.spotify.token") != null;
    }

    setToken(token) {
        appStore.set("music_integration.spotify.token", token);

        if (token) {
            if (!taskId) {
                pollSpotify();
            }
        } else {
            clearTimeout(taskId);
            taskId = null;
            this.playbackState = MusicStates.INACTIVE;
        }
    }

    async loginOauth(code) {
        const response = await fetch(`${AUTH_PROXY_URL}?code=${code}`);
        const authResult = await response.json();

        if (authResult.error) {
            throw authResult.error;
        } else {
            this.setToken(authResult.refresh_token);
        }
    }

}

const integrationInstance = new SpotifyIntegration();
let accessToken;
let taskId;

async function pollSpotify() {
    try {
        if (!accessToken) {
            const refreshToken = appStore.get("music_integration.spotify.token");
            const auth = await (await fetch(`${AUTH_PROXY_URL}?refresh_token=${refreshToken}`)).json();

            if (auth.error) {
                integrationInstance.setToken(null);
            } else {
                accessToken = auth.access_token;

                if (auth.refresh_token) {
                    appStore.set("music_integration.spotify.token", auth.refresh_token);
                }

                integrationInstance.spotifyProfile = await (await fetch("https://api.spotify.com/v1/me", {
                    headers: {
                        "content-type": "application/json",
                        authorization: "Bearer " + accessToken
                    }
                })).json();
            }
        }

        const response = await fetch("https://api.spotify.com/v1/me/player", {
            headers: {
                "content-type": "application/json",
                authorization: "Bearer " + accessToken
            }
        });

        if (response.status == 401) {
            accessToken = null;
            this.pollSpotify();
            return;
        } else if (response.status == 200) {
            const player = await response.json();

            if (player.item) {
                const songLink = player.item.external_urls.spotify;
                const isPlaying = player.is_playing;

                const playbackState = isPlaying ? MusicStates.PLAYING : MusicStates.PAUSED;

                if (
                    !integrationInstance.currentTrack ||
                    (songLink != integrationInstance.currentTrack.link) ||
                    (playbackState != integrationInstance.playbackState)
                ) {
                    const image = player.item.album.images[0].url;
                    const album = player.item.album.name;
                    const title = player.item.name.replace(/(\(ft.*\))|(\(feat.*\))/gi, ""); // Remove (feat. ...)
                    let artists = [];

                    player.item.artists.forEach((artist) => {
                        artists.push(artist.name);
                    });

                    integrationInstance.currentTrack = new MusicTrack(title, artists, album, image, songLink);
                    integrationInstance.playbackState = playbackState;
                }
            }
        }
    } catch (e) {
        console.error("[Spotify]", "Failed to poll Spotify's Api:\n", e);
    }

    taskId = setTimeout(pollSpotify, 10 * 1000);
}

if (integrationInstance.isEnabled()) {
    pollSpotify();
}

export default integrationInstance;