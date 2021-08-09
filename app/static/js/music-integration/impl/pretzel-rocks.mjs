import { appStore } from "../../caffeinated.mjs";
import Auth from "../../auth.mjs";
import { MusicIntegration, MusicTrack, MusicStates } from "../music-util.mjs";

const PRETZEL_ALBUM_ART = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mOU3rzkPwAE1gJzt/4W2gAAAABJRU5ErkJggg==";
const PRETZEL_ENDPOINT = "https://api.pretzel.tv/playing/twitch";

class PretzelIntegration extends MusicIntegration {

    constructor() {
        super("Pretzel", "PRETZEL_ROCKS");
    }

    isEnabled() {
        return appStore.get("music_integration.pretzel_rocks.enabled");
    }

    setEnabled(enabled) {
        appStore.set("music_integration.pretzel_rocks.enabled", enabled);

        if (enabled) {
            if (!taskId) {
                pollPretzelRocks();
            }
        } else {
            clearTimeout(taskId);
            taskId = null;
            this.playbackState = MusicStates.INACTIVE;
        }
    }

    isLoggedIn() {
        if (Auth.getSignedInPlatforms()?.TWITCH?.userData) {
            return true;
        } else {
            this.playbackState = MusicStates.INACTIVE;
            return false;
        }
    }

}

let taskId;
const integrationInstance = new PretzelIntegration();

async function pollPretzelRocks() {
    try {
        if (integrationInstance.isLoggedIn()) {
            const twitchChannelId = Auth.getSignedInPlatforms().TWITCH.userData.streamer.channel_id;

            const result = await fetch(`${PRETZEL_ENDPOINT}/${twitchChannelId}`);

            if (result.ok) {
                // "Now Playing: Arcade by Stesso -> https://prtzl.io/PDHXD5z6enygxyFmE"
                let nowPlaying = await result.text();

                // "Arcade by Stesso -> https://prtzl.io/PDHXD5z6enygxyFmE"
                nowPlaying = nowPlaying.substring("Now Playing: ".length);

                // Separate out the link
                nowPlaying = nowPlaying.split(" -> https://");
                const songLink = "https://" + nowPlaying[1];
                nowPlaying = nowPlaying[0]; // "Arcade by Stesso"

                // Split out the title and artist(s)
                nowPlaying = nowPlaying.split(" by ");

                const title = nowPlaying[0];
                const artists = [nowPlaying[1]];

                if (
                    !integrationInstance.currentTrack ||
                    (integrationInstance.currentTrack.link != songLink)
                ) {
                    integrationInstance.currentTrack = new MusicTrack(title, artists, null, PRETZEL_ALBUM_ART, songLink);
                    integrationInstance.playbackState = MusicStates.PLAYING;
                }
            } else {
                if (result.status = 404) {
                    // They haven't used Pretzel yet.
                    integrationInstance.playbackState = MusicStates.INACTIVE;
                } else {
                    throw (await result.json());
                }
            }
        }
    } catch (e) {
        console.error("[PretzelRocks]", "Failed to poll Pretzel's Api:\n", e);
    }

    taskId = setTimeout(pollPretzelRocks, 20 * 1000);
}

if (integrationInstance.isEnabled()) {
    pollPretzelRocks();
}

export default integrationInstance;