<script>
    import PageAttributes from "../../components/page-attributes.svelte";
    import ShowSideBar from "../../components/displaymodes/show-sidebar.svelte";

    import CreationDropdownCategory from "../../components/widget-manager/creation-dropdown-category.svelte";

    import { onMount } from "svelte";

    const DEFAULT_MODULE_ICON = "grid"; // https://feathericons.com/?query=grid

    let widgetCategories = {
        alerts: [],
        labels: [],
        interaction: [],
        goals: [],
        other: []
    };

    let widgets = [];

    async function renderCreationDisplay() {
        let _widgetCategories = {
            alerts: [],
            labels: [],
            interaction: [],
            goals: [],
            other: []
        };

        // We rely on the global Modules instance
        for (const dynamicHolder of Object.values(Modules.getDynamicModuleHolders())) {
            const { declaration } = dynamicHolder;

            (
                _widgetCategories[declaration.category] ||
                _widgetCategories.other
            ).push({
                name: declaration.label,
                create: () => {
                    dynamicHolder.create("New Widget");
                }
            });
        }

        // This forces svelte to rerender.
        widgetCategories = _widgetCategories;
    }

    function renderWidgetTiles() {
        const staticModules = Object.values(Modules.getStaticModules());
        const dynamicModules = Object.values(Modules.getDynamicModules());

        widgets = [
            ...staticModules,
            ...dynamicModules
        ];
    }

    async function render() {
        await renderCreationDisplay();
        renderWidgetTiles();

        feather.replace();
    }

    onMount(async () => {
        await render();
    });
</script>

<style>
    #widget-creation-dropdown {
        position: fixed;
        bottom: .75rem;
        left: calc(var(--side-bar-width) + .75rem);
    }

    #widget-creation-dropdown>.dropdown>.dropdown-trigger {
        text-align: left;
        width: 3.5rem;
    }

    #widget-creation-dropdown-content>.dropdown-content {
        padding-top: 2px;
        padding-bottom: 2px;
    }

    #widget-creation-dropdown-content>.dropdown-content>.dropdown-item {
        padding: 0;
    }

    #widget-creation-dropdown>.dropdown:hover>.dropdown-menu {
        display: block;
    }

    .dropdown-divider {
        margin: 2px;
    }

    .ghost-button {
        padding: 0;
        padding-left: .5em;
        width: 11rem;
        justify-content: left;
        border: none;
    }

    #all-widgets {
        padding: 1em;
    }

    .widget-tile {
        display: inline-block;
        width: 150px;
        height: 150px;
        margin: .5em;
    }

    :global(.widget-tile svg) {
        margin-top: .75em;
        margin-bottom: .75em;
        width: 4em;
        height: 4em;
    }

    .widget-tile p {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
</style>

<PageAttributes allowNavigateBackwards="true" />
<ShowSideBar />

<div class="has-text-centered">
    <!-- All widgets -->
    <div id="all-widgets">
        {#each widgets as widget}
        <a class="button widget-tile" href="/pages/edit-widget?widget={widget.getFullId()}">
            <i data-feather="{widget.moduleDeclaration.icon}" aria-hidden="true"></i>
            <p>
                {widget.name}
            </p>
        </a>
        <script>
            feather.replace();
        </script>
        {/each}
    </div>

    <div id="widget-creation-dropdown">
        <div class="dropdown is-left is-up">
            <div class="dropdown-trigger">
                <button class="button" aria-haspopup="true" aria-controls="widget-creation-dropdown-content">
                    <span class="icon is-small">
                        <i data-feather="plus" aria-hidden="true"></i>
                    </span>
                </button>
            </div>
            <div class="dropdown-menu" id="widget-creation-dropdown-content" role="menu">
                <div class="dropdown-content">

                    <!-- "Alerts" Dropdown -->
                    <div class="dropdown-item">
                        <CreationDropdownCategory name="Alerts" icon="bell">
                            {#each widgetCategories.alerts as item}
                            <div class="dropdown-item">
                                <button class="button ghost-button" on:click="{item.create}" style="width: 11rem;">
                                    {item.name}
                                </button>
                            </div>
                            {/each}
                        </CreationDropdownCategory>
                    </div>

                    <hr class="dropdown-divider">

                    <!-- "Labels" Dropdown -->
                    <div class="dropdown-item">
                        <CreationDropdownCategory name="Labels" icon="type">
                            {#each widgetCategories.labels as item}
                            <div class="dropdown-item">
                                <button class="button ghost-button" on:click="{item.create}" style="width: 11rem;">
                                    {item.name}
                                </button>
                            </div>
                            {/each}
                        </CreationDropdownCategory>
                    </div>

                    <hr class="dropdown-divider">

                    <!-- "Interaction" Dropdown -->
                    <div class="dropdown-item">
                        <CreationDropdownCategory name="Interaction" icon="message-circle">
                            {#each widgetCategories.interaction as item}
                            <div class="dropdown-item">
                                <button class="button ghost-button" on:click="{item.create}" style="width: 11rem;">
                                    {item.name}
                                </button>
                            </div>
                            {/each}
                        </CreationDropdownCategory>
                    </div>

                    <hr class="dropdown-divider">

                    <!-- "Goals" Dropdown -->
                    <div class="dropdown-item">
                        <CreationDropdownCategory name="Goals" icon="bar-chart">
                            {#each widgetCategories.goals as item}
                            <div class="dropdown-item">
                                <button class="button ghost-button" on:click="{item.create}" style="width: 11rem;">
                                    {item.name}
                                </button>
                            </div>
                            {/each}
                        </CreationDropdownCategory>
                    </div>

                    {#if widgetCategories.other.length > 0}
                    <hr class="dropdown-divider">

                    <!-- "Other" Dropdown -->
                    <div class="dropdown-item">
                        <CreationDropdownCategory name="Other" icon="droplet">
                            {#each widgetCategories.other as item}
                            <div class="dropdown-item">
                                <button class="button ghost-button" on:click="{item.create}" style="width: 11rem;">
                                    {item.name}
                                </button>
                            </div>
                            {/each}
                        </CreationDropdownCategory>
                    </div>
                    <script>
                        feather.replace();
                    </script>
                    {/if}

                </div>
            </div>
        </div>
    </div>
</div>