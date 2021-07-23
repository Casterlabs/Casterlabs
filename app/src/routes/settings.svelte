<script>
    // Meta
    import PageAttributes from "../components/page-attributes.svelte";
    import HideSideBar from "../components/displaymodes/hide-sidebar.svelte";

    // Settings Content
    import SettingsContainer from "../components/settings/settings-container.svelte";
    import AccountSettings from "../components/settings/account-settings.svelte";
    import AppearanceSettings from "../components/settings/appearance-settings.svelte";
    import StatusIntegrationSettings from "../components/settings/status-integration-settings.svelte";

    const settingsContainerContents = [
        {
            name: "User Settings",
            type: "section"
        },
        {
            name: "Accounts",
            id: "accounts",
            type: "category"
        },
        {
            name: "App Settings",
            type: "section"
        },
        {
            name: "Appearance",
            id: "appearance",
            type: "category"
        },
        {
            name: "Other",
            type: "section"
        },
        {
            name: "Status Integration",
            id: "status_integration",
            type: "category"
        }
    ];
</script>

<PageAttributes title="Settings" allowNavigateBackwards="true" />
<HideSideBar />

<style>
    .settings-close {
        position: absolute;
        top: .75em;
        right: .75em;
        color: inherit;
        z-index: 2;
    }

    .settings-close svg {
        height: 28px;
        width: 28px;
    }

    /* Pad out the settings page */
    :global(.settings-navigate) {
        padding-top: 2em;
    }

    :global(.settings-content) {
        top: 2em !important;
    }
</style>

<div class="settings-contents no-select">
    <a class="settings-close fade-on-hover" onclick="history.back()">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-x-circle">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="15" y1="9" x2="9" y2="15"></line>
            <line x1="9" y1="9" x2="15" y2="15"></line>
        </svg>
    </a>

    <div class="settings-body">
        <SettingsContainer categories="{settingsContainerContents}">
            <section class="hidden" data-id="accounts">
                <AccountSettings />
            </section>
            <section class="hidden" data-id="appearance">
                <AppearanceSettings />
            </section>
            <section class="hidden" data-id="status_integration">
                <StatusIntegrationSettings />
            </section>
        </SettingsContainer>
    </div>
</div>