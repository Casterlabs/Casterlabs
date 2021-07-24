<style>
    .button {
        width: 200px;
        margin-top: 1px;
        overflow: hidden;
        color: #dbdbdb;
        transition: .5s;
        background-color: #8838ff;
    }

    .button:hover {
        transition: .5s;
        background-color: #7b24ff;
    }

    .button span {
        z-index: 2;
    }

    .platform-logo {
        position: absolute;
        top: 10px;
        left: 14px;
        width: 19px;
    }

    .platform-logo svg {
        width: 100%;
        height: 100%;
    }
</style>

<div class="no-select signin-button">
    <a class="button has-text-centered" href="/signin/twitch">
        <div class="platform-logo">
            <img src="/img/platforms/twitch/icon.svg" alt="Twitch Logo" />
        </div>
        <span>
            Twitch
        </span>
    </a>
</div>