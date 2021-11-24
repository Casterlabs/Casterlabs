<script>
    import AccountBox from "./account-settings/account-box.svelte";

    import { onMount, onDestroy } from "svelte";

    let eventHandler;

    let spotifyData = null;
    let pretzelData = null;
    let activePlayback = null;

    function parseBridgeData(data) {
        console.debug("[MusicServicesSettings]", data);
        activePlayback = data.activePlayback;
        spotifyData = data.musicServices.spotify;
        pretzelData = data.musicServices.pretzel;
    }

    onDestroy(() => {
        eventHandler?.destroy();
    });

    onMount(async () => {
        eventHandler = Bridge.createThrowawayEventHandler();
        eventHandler.on("music:update", parseBridgeData);
        parseBridgeData((await Bridge.query("music")).data);
    });

    function updatePretzel(e) {
        const enabled = e.target.checked;
        Bridge.emit("music:settings-update", {
            platform: "pretzel",
            settings: {
                enabled: enabled
            }
        });
    }

    function signOutSpotify() {
        Bridge.emit("music:signout", { platform: "spotify" });
    }
</script>

<div class="no-select">
    <p>
        {#if activePlayback}
            &nbsp;Now Playing:
            <span style="user-select: all !important;">
                {activePlayback.currentTrack.title}
            </span>
            &bull;
            <span style="user-select: all !important;">
                {activePlayback.currentTrack.artists.join(", ")}
            </span>
        {/if}
    </p>

    <br />

    <div id="accounts">
        <!-- These are in order of preference btw -->
        {#if spotifyData}
            <AccountBox
                platform="spotify"
                platformName="Spotify"
                signInLink="/signin/spotify"
                bind:accountName={spotifyData.accountName}
                bind:accountLink={spotifyData.accountLink}
                bind:isSignedIn={spotifyData.isSignedIn}
                on:signout={signOutSpotify}
            />
        {/if}
        {#if pretzelData}
            <AccountBox
                platform="pretzelrocks"
                platformName="Pretzel"
                signInLink="/signin/twitch"
                bind:accountName={pretzelData.accountName}
                bind:accountLink={pretzelData.accountLink}
                bind:isSignedIn={pretzelData.isSignedIn}
                canSignOut={false}
            >
                {#if pretzelData.isSignedIn}
                    <label class="checkbox">
                        <input type="checkbox" bind:checked={pretzelData.settings.enabled} on:change={updatePretzel} />
                        Enabled
                    </label>
                {/if}
            </AccountBox>
        {/if}
    </div>
</div>

<style>
    #accounts {
        margin-right: 55px;
    }
</style>
