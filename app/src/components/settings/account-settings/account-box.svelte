<script>
    export let platform;
    export let platformName;
</script>

<style>
    .box {
        position: relative !important;
        margin-bottom: .9rem !important;
    }

    .platform-logo {
        position: absolute;
        top: 23px;
        left: 14px;
        width: 19px;
    }

    .open-channel {
        color: inherit !important;
        margin-left: 1.4em;
        cursor: default;
    }

    :global(.linked) .open-channel {
        text-decoration: underline;
        cursor: pointer !important;
    }

    .tag {
        height: 20px;
        transform: translateY(-1px);
        display: none;
    }

    :global(.linked) .tag {
        display: inline-block !important;
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

    :global(.linked) .signin-button {
        display: none !important;
    }

    .signout-button {
        display: none;
        position: absolute;
        top: 50%;
        right: 1.5em;
        width: 55px;
        transform: translateY(-50%);
        text-align: center;
    }

    :global(.linked) .signout-button {
        display: inline-block !important;
    }
</style>

<div id="account-{platform}" class="box">
    <div class="platform-logo">
        <img src="/img/platforms/{platform}/icon.svg" alt="{platformName} Logo" />
    </div>
    <a class="open-channel" rel="external">
        {platformName}
    </a>
    <span class="tag allow-select streamer-name">
        casterlabs
    </span>
    <a href="/login/{platform}" class="tag is-success signin-button">
        Link
    </a>
    <a class="tag is-danger signout-button">
        Unlink
    </a>
</div>