<script>
    // Meta
    import { setPageProperties } from "./__layout.svelte";
    import { onMount } from "svelte";

    import SettingsContainer from "../components/settings/settings-container.svelte";

    // Settings Content
    import AccountSettings from "../components/settings/account-settings.svelte";
    import MusicServicesSettings from "../components/settings/music-services-settings.svelte";
    import AppearanceSettings from "../components/settings/appearance-settings.svelte";
    // import StatusIntegrationSettings from "../components/settings/status-integration-settings.svelte";
    import AboutSettings from "../components/settings/about-settings.svelte";

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: true
    });

    const settingsContainerContents = [
        {
            name: "App Settings",
            type: "section"
        },
        {
            name: "Appearance",
            id: "appearance",
            type: "category",
            component: AppearanceSettings
        },
        {
            name: "User Settings",
            type: "section"
        },
        {
            name: "Accounts",
            id: "accounts",
            type: "category",
            component: AccountSettings
        },
        {
            name: "Music Services",
            id: "music_services",
            type: "category",
            component: MusicServicesSettings
        },
        {
            name: "Other",
            type: "section"
        },
        {
            name: "About",
            id: "about",
            type: "category",
            component: AboutSettings
        }
        // {
        //     name: "Status Integration",
        //     id: "status_integration",
        //     type: "category",
        //     component: StatusIntegrationSettings
        // }
    ];

    onMount(() => {
        document.title = "Settings";
    });
</script>

<div class="settings-contents">
    <!-- svelte-ignore a11y-missing-attribute -->
    <a class="settings-close fade-on-hover" onclick="history.back()">
        <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="feather feather-x-circle"
        >
            <circle cx="12" cy="12" r="10" />
            <line x1="15" y1="9" x2="9" y2="15" />
            <line x1="9" y1="9" x2="15" y2="15" />
        </svg>
    </a>

    <div class="settings-body">
        <SettingsContainer categories={settingsContainerContents} />
    </div>
</div>

<style>
    .settings-close {
        position: absolute;
        top: 0.75em;
        right: 0.75em;
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
