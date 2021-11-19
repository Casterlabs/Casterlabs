<script>
    import AccountBox from "./account-settings/account-box.svelte";

    import { onMount, onDestroy } from "svelte";

    let pollingIds = [];
    let services = {
        spotify: {
            accountName: "",
            accountLink: "#",
            isSignedIn: false
        },
        pretzelrocks: {
            accountName: "",
            accountLink: "#",
            isSignedIn: false
        }
    };

    let currentTrack;

    // onMount for Pretzel.
    onMount(() => {
        const pretzel = MusicIntegration.PRETZEL_ROCKS;

        const pollId = setInterval(() => {
            const isSignedIn = pretzel.isLoggedIn();

            if (isSignedIn) {
                const { displayname, link } = Auth.getSignedInPlatforms().TWITCH.userData.streamer;
                services.pretzelrocks = {
                    accountName: `Twitch: ${displayname}`,
                    accountLink: link,
                    isSignedIn: isSignedIn
                };
            } else {
                services.pretzelrocks = {
                    accountName: "",
                    accountLink: "#",
                    isSignedIn: false
                };
            }
        }, 500);
        pollingIds.push(pollId);
    });

    let spotify;

    // onMount for Spotify.
    onMount(() => {
        spotify = MusicIntegration.SPOTIFY;

        const pollId = setInterval(() => {
            const isSignedIn = spotify.isEnabled() && spotify.spotifyProfile;

            if (isSignedIn) {
                const { display_name, external_urls } = MusicIntegration.SPOTIFY.spotifyProfile;
                services.spotify = {
                    accountName: display_name,
                    accountLink: external_urls.spotify,
                    isSignedIn: isSignedIn
                };
            } else {
                services.spotify = {
                    accountName: "",
                    accountLink: "#",
                    isSignedIn: false
                };
            }
        }, 500);
        pollingIds.push(pollId);
    });

    function signOutSpotify() {
        spotify.setToken(null);
    }

    // onMount for currentPlayback.
    onMount(() => {
        const pollId = setInterval(() => {
            currentTrack = MusicIntegration.getCurrentPlayback()?.currentTrack;
        }, 500);
        pollingIds.push(pollId);
    });

    onDestroy(() => {
        pollingIds.forEach(clearInterval);
    });
</script>

<div class="no-select">
    <p>
        {#if currentTrack}
            &nbsp;Now Playing: {currentTrack.title} - {currentTrack.artists.join(", ")}
        {/if}
    </p>

    <br />

    <div id="accounts">
        <!-- i know, i know, it looks messy but it works so well. -->
        <AccountBox platform="pretzelrocks" platformName="Pretzel" signInLink="/signin/twitch" bind:accountName={services.pretzelrocks.accountName} bind:accountLink={services.pretzelrocks.accountLink} bind:isSignedIn={services.pretzelrocks.isSignedIn} canSignOut={false} />
        <AccountBox platform="spotify" platformName="Spotify" signInLink="/signin/spotify" bind:accountName={services.spotify.accountName} bind:accountLink={services.spotify.accountLink} bind:isSignedIn={services.spotify.isSignedIn} on:signout={signOutSpotify} />
    </div>
</div>

<style>
    #accounts {
        margin-right: 55px;
    }
</style>
