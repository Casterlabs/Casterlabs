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
    // import OAuthSignin from "../../platforms/oauth-signin.svelte";

    import { onMount } from "svelte";

    let spotify = null;
    let isLoggedIn = false;

    onMount(() => {
        spotify = MusicIntegration.SPOTIFY;
    });

    setInterval(() => {
        isLoggedIn = spotify && spotify.isEnabled();
    }, 100);

    function trySignin() {
        if (isLoggedIn) {
            spotify.setToken(null);
        } else {
            // L'Chaim
        }
    }
</script>

<div class="no-select login-button">
    <a class="button has-text-centered" on:click="{trySignin}">
        <div class="platform-logo">
            <img src="/img/services/spotify/icon.svg" alt="Pretzel Logo" />
        </div>
        <span>
            {#if isLoggedIn}
            Spotify ({MusicIntegration.SPOTIFY.spotifyProfile.display_name})
            {:else}
            Click to sign-in to Spotify
            {/if}
        </span>
    </a>

    <script type="module">

    </script>
</div>