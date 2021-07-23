<style>
    .button {
        width: 200px;
        margin-top: 1px;
        overflow: hidden;
        color: #dbdbdb;
        transition: .5s;
        background: linear-gradient(45deg, #8439af 15%, #fc3537 65%);
    }

    .button:hover {
        transition: .5s;
        background: linear-gradient(45deg, #7a34a2 15%, #fc2226 65%);
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
    <a class="button has-text-centered" href="/login/brime">
        <div class="platform-logo">
            <img src="/img/platforms/brime/icon.svg" alt="Brime Logo" />
        </div>
        <span>
            Brime
        </span>
    </a>
</div>