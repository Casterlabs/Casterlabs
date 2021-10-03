<script>
    import { onMount } from "svelte";

    let pretzel = null;
    let isLoggedIn = false;

    onMount(() => {
        pretzel = MusicIntegration.PRETZEL_ROCKS;
    });

    setInterval(() => {
        isLoggedIn = pretzel && pretzel.isLoggedIn();
    }, 100);

    function trySignin() {
        if (!isLoggedIn) {
            location.href = "/signin/twitch";
        }
    }
</script>

<div class="no-select login-button">
    <button class="button has-text-centered" on:click={trySignin}>
        <div class="platform-logo">
            <img src="/img/services/pretzelrocks/icon.svg" alt="Pretzel Logo" />
        </div>
        <span>
            {#if isLoggedIn}
                Pretzel ({window.Auth.getSignedInPlatforms().TWITCH.userData.streamer.displayname})
            {:else}
                Click to sign-in to Pretzel
            {/if}
        </span>
    </button>

    <script type="module">
    </script>
</div>

<style>
    .button {
        width: 300px;
        margin-top: 1px;
        overflow: hidden;
        color: #dbdbdb;
        transition: 0.5s;
        background-color: #19b2a4;
    }

    .button:hover {
        transition: 0.5s;
        background-color: #179b90;
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
        /* ;-; */
        filter: invert(1);
    }
</style>
