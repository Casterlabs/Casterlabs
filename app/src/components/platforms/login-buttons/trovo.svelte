<style>
    .button {
        width: 200px;
        margin-top: 1px;
        overflow: hidden;
        color: #dbdbdb;
        transition: .5s;
        background-color: #149b53;
    }

    .button:hover {
        transition: .5s;
        background-color: #118849;
    }

    .button span {
        z-index: 2;
    }

    .platform-logo {
        position: absolute;
        top: 8px;
        left: 11px;
        width: 23px;
    }

    .platform-logo svg {
        width: 100%;
        height: 100%;
    }
</style>

<div class="no-select login-button">
    <a class="button has-text-centered" href="/login/trovo">
        <div class="platform-logo">
            <img src="/img/platforms/trovo/icon.svg" alt="Trovo Logo" />
        </div>
        <span>
            Trovo
        </span>
    </a>
</div>