<script>
    export let platform;
    export let platformName;
    export let signInLink;
    export let accountName;
    export let accountLink;
    export let isSignedIn = false;
    export let canSignOut = true;

    import { createEventDispatcher } from "svelte";

    const dispatch = createEventDispatcher();

    function sendSignout() {
        dispatch("signout", {
            platform: platform
        });
    }
</script>

<!-- svelte-ignore a11y-missing-attribute -->
<div id="account-{platform}" class="box">
    <div class="platform-logo">
        <img src="/img/services/{platform}/icon.svg" alt="{platformName} Logo" />
    </div>

    {#if isSignedIn}
        <a href={accountLink} class="platform-name open-channel" rel="external">
            {platformName}
        </a>

        <span class="tag streamer-name" style="user-select: all !important;"> {accountName} </span>

        {#if canSignOut}
            <a on:click={sendSignout} class="tag is-danger signout-button"> Unlink </a>
        {/if}
    {:else}
        <span class="platform-name">
            {platformName}
        </span>

        <a href="{signInLink}?homeGoBack=1" class="tag is-success signin-button"> Link </a>
    {/if}

    <span style="margin-left: 10px;"><slot /> </span>
</div>

<style>
    :global(#account-caffeine .platform-logo) {
        top: 22px;
        left: 13px;
        width: 23px;
    }

    .box {
        position: relative !important;
        margin-bottom: 0.9rem !important;
    }

    :global(:not(.bulma-dark-mode)) .platform-logo img {
        filter: invert(0.8); /* Lazy Way */
    }

    .platform-logo {
        position: absolute;
        top: 23px;
        left: 14px;
        width: 19px;
    }

    .platform-name {
        color: inherit !important;
        margin-left: 1.4em;
    }

    .open-channel {
        text-decoration: underline;
    }

    .tag {
        display: inline-block !important;
        height: 20px;
        transform: translateY(-1px);
    }

    .signin-button {
        display: inline-block;
        position: absolute;
        top: 50%;
        right: 1.5em;
        width: 55px;
        transform: translateY(-50%);
        text-align: center;
    }

    .signout-button {
        display: inline-block !important;
        position: absolute;
        top: 50%;
        right: 1.5em;
        width: 55px;
        transform: translateY(-50%);
        text-align: center;
    }
</style>
