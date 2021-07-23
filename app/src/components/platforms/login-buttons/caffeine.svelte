<style>
    .button {
        width: 200px;
        margin-top: 1px;
        overflow: hidden;
        color: #dbdbdb;
        transition: .5s;
        background-color: #0000FF;
    }

    .button:hover {
        transition: .5s;
        background-color: #0000cc;
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

    .platform-logo svg {
        width: 100%;
        height: 100%;
    }
</style>

<div class="no-select login-button">
    <a class="button has-text-centered" href="/login/caffeine">
        <div class="platform-logo">
            <img src="/img/platforms/caffeine/icon.svg" alt="Caffeine Logo" />
        </div>
        <span>
            Caffeine
        </span>
    </a>
</div>