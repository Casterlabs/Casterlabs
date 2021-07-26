<script>
    import ToggleableDropdown from "./toggleable-dropdown.svelte";
</script>

<style>
    .sidebar-container {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        overflow: hidden;
    }

    .sidebar {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        overflow-y: auto;
    }

    :global(.dropdown-content) {
        background-color: transparent !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        padding: 0;
    }

    :global(.dropdown-trigger>.button) {
        padding-left: 7.5px;
        padding-right: 7.5px;
        background-color: transparent !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        padding: 0;
        border-color: transparent !important;
        border-width: 0 !important;
    }


    :global(.dropdown-trigger>.button:hover) {
        background-color: rgba(100, 100, 100, .05) !important;
    }

    .sidebar-section {
        margin-bottom: 0 !important;
    }

    .sidebar-section:not(:first-child) {
        margin-top: 15px;
    }

    .sidebar-category-button {
        padding-left: 7.5px;
        padding-right: 7.5px;
        color: unset !important;
        width: 100%;
        margin-right: 10px;
        border-radius: 4px;
        display: block;
    }

    .sidebar-category-button span {
        margin-left: 10px;
    }

    .sidebar-category-button:hover {
        background-color: rgba(100, 100, 100, .05);
    }

    .sidebar-category-button:global(.is-selected) {
        background-color: rgba(100, 100, 100, .2);
    }
</style>

<div class="sidebar-container">
    <div class="sidebar has-text-left">

        <div class="dropdown-container">
            <ToggleableDropdown>
                <div class="sidebar-section" slot="dropdown-name">
                    <span class="title is-size-6">
                        Widgets
                    </span>
                </div>
                <div slot="dropdown-content">
                    <a class="sidebar-category-button is-size-6">
                        <span>
                            Donation Widget
                        </span>
                    </a>
                </div>
            </ToggleableDropdown>
        </div>

    </div>
</div>