<script>
    import { setPageProperties } from "../__layout.svelte";

    import FormElement from "../../components/form/form-element.svelte";

    import { onMount } from "svelte";

    setPageProperties({
        showSideBar: false,
        pageTitle: "Widget Manager",
        allowNavigateBackwards: true
    });

    let widget = null;

    // Holy shit this is so ugly.
    // For some reason svelte won't always render the component properly.
    // Sometimes it renders the component, but the data is not updated.
    let blanking = false;

    let widgetCategories = [];
    let currentWidgetCategory = null;

    function switchCategory(category) {
        blanking = true;

        setTimeout(() => {
            currentWidgetCategory = category;
            blanking = false;
        }, 50);
    }

    onMount(async () => {
        const widgetId = location.href.split("?widget=")[1];

        widget = Modules.getDynamicModules()[widgetId] || Modules.getStaticModules()[widgetId];

        widgetCategories = widget.moduleDeclaration.settings;
        currentWidgetCategory = widgetCategories[0];
    });
</script>

{#if widget}
    <div class="has-text-centered">
        <h1 class="title is-5" style="padding-top: 10px; padding-bottom: 10px;">
            {widget.name}
        </h1>

        <div class="tabs">
            <!-- svelte-ignore a11y-missing-attribute -->
            <ul style="justify-content: center !important;">
                {#each widgetCategories as widgetCategory}
                    {#if widgetCategory == currentWidgetCategory}
                        <li class="is-active">
                            <a>
                                {widgetCategory.label}
                            </a>
                        </li>
                    {:else}
                        <li>
                            <a on:click={switchCategory(widgetCategory)}>
                                {widgetCategory.label}
                            </a>
                        </li>
                    {/if}
                {/each}
            </ul>
        </div>

        <div class="allow-select has-text-left">
            {#if !blanking}
                {#each currentWidgetCategory.items as widgetSettingsOption}
                    <div class="columns">
                        <div class="column" style="max-width: 260px; min-width: 260px;">
                            <span class="has-text-weight-medium">
                                {widgetSettingsOption.label}
                            </span>
                        </div>
                        <div class="column">
                            <FormElement module={widget} itemDeclaration={widgetSettingsOption} />
                        </div>
                    </div>
                {/each}
            {/if}
        </div>
    </div>
{/if}
