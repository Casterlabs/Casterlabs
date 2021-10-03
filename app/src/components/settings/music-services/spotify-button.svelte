<style>
    .button {
        width: 300px;
        margin-top: 1px;
        overflow: hidden;
        color: #dbdbdb;
        transition: .5s;
        background-color: #1DB954;
    }

    .button:hover {
        transition: .5s;
        background-color: #189945;
    }


    .button span {
        z-index: 2;
    }

    .platform-logo {
        position: absolute;
        top: 7px;
        left: 10px;
        width: 24px;
    }

    .platform-logo img {
        width: 100%;
        height: 100%;
    }
</style>

<script>
    import { onMount } from "svelte";

    let spotify = null;
    let isLoggedIn = false;

    onMount(() => {
        spotify = MusicIntegration.SPOTIFY;
    });

    setInterval(() => {
        isLoggedIn = spotify && spotify.isEnabled() && spotify.spotifyProfile;
    }, 100);

    function signOutSpotify() {
        spotify.setToken(null);
    }
</script>

<div class="no-select login-button">
    {#if isLoggedIn}
    <button class="button has-text-centered" on:click="{signOutSpotify}">
        <div class="platform-logo">
            <img src="/img/services/spotify/icon.svg" alt="Spotify Logo" />
        </div>
        <span>
            Spotify ({window.MusicIntegration.SPOTIFY.spotifyProfile.display_name})
        </span>
    </button>
    {:else}
    <a class="button has-text-centered" href="/signin/spotify">
        <div class="platform-logo">
            <img src="/img/services/spotify/icon.svg" alt="Spotify Logo" />
        </div>
        <span>
            Click to sign-in to Spotify
        </span>
    </a>
    {/if}

    <script type="module">

    </script>
</div>