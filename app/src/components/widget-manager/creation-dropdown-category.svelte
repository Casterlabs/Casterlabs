<script>
    export let name;
    export let icon;
</script>

<style>
    .dropdown-menu {
        position: absolute;
        left: 100% !important;
        bottom: -2px !important;
        padding-bottom: 0 !important;
        padding-left: 13px;
    }

    .dropdown-content {
        padding-top: 2px;
        padding-bottom: 2px;
    }

    :global(.dropdown-content>.dropdown-item) {
        padding: 0;
    }

    :global(.dropdown-content>.dropdown-item>.button) {
        width: 8rem;
        padding: 0;
        padding-left: 10px;
    }

    :global(.dropdown-divider) {
        margin: 2px;
    }

    .ghost-button {
        padding: 0;
        padding-left: .5em;
        width: 11rem;
        justify-content: left;
        border: none;
    }

    .right-chevron {
        position: absolute;
        right: 1em;
    }
</style>

<div class="dropdown is-hoverable">
    <div class="dropdown-trigger">
        <button class="button ghost-button" aria-haspopup="true" aria-controls="{name}-dropdown">
            <span class="icon-text">
                <span class="icon is-small">
                    <i data-feather="{icon}" aria-hidden="true"></i>
                </span>
                <span>
                    {name}
                </span>
            </span>
            <span class="icon is-small right-chevron">
                <i data-feather="chevron-right" aria-hidden="true"></i>
            </span>
        </button>
    </div>
    <div class="dropdown-menu" id="{name}-dropdown" role="menu">
        <div class="dropdown-content">
            <slot></slot>
        </div>
    </div>
</div>