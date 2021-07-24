<style>
    .button {
        width: 200px;
        margin-top: 1px;
        overflow: hidden;
        color: #dbdbdb;
        transition: .5s;
        background-color: #0d1524;
    }

    .button:hover {
        transition: .5s;
        background-color: #0c1322;
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

<div class="no-select signin-button">
    <a class="button has-text-centered" href="/signin/glimesh">
        <div class="platform-logo">
            <img src="/img/platforms/glimesh/icon.svg" alt="Glimesh Logo" />
        </div>
        <span>
            Glimesh
        </span>
    </a>
</div>