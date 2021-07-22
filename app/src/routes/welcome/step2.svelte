<script>
    // Meta
    import PageAttributes from "../../components/page-attributes.svelte";
    import HideSideBar from "../../components/displaymodes/hide-sidebar.svelte";

    // Settings Content
    import SettingsContainer from "../../components/settings/settings-container.svelte";
    import AppearanceSettings from "../../components/settings/appearance-settings.svelte";
    import StatusIntegrationSettings from "../../components/settings/status-integration-settings.svelte";

    const settingsContainerContents = [
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

<PageAttributes />
<HideSideBar />

<style>
    .welcome-settings {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
    }

    .welcome-title {
        margin-top: 3.5em;
        /* margin-left: 175px; */
    }

    /* Pad out the settings page */
    :global(.settings-navigate) {
        padding-top: 9em;
    }

    :global(.settings-content) {
        top: 9em !important;
    }

    #next-page-button {
        position: absolute;
        bottom: 1em;
        right: 1em;
    }
</style>

<div class="welcome-contents no-select">
    <div class="welcome-title has-text-centered">
        <h1 class="title is-4">
            Let's customize Caffeinated.
        </h1>
    </div>

    <div class="welcome-settings">
        <SettingsContainer categories="{settingsContainerContents}">
            <section class="hidden" data-id="appearance">
                <AppearanceSettings />
            </section>
            <section class="hidden" data-id="status_integration">
                <StatusIntegrationSettings />
            </section>
        </SettingsContainer>
    </div>

    <a id="next-page-button" class="button" href="/login">
        Next <i data-feather="arrow-right"></i>
    </a>
</div>