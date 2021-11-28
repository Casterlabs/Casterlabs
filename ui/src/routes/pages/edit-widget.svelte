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

    let widgetSections = [];
    let currentWidgetSection = null;

    function switchCategory(category) {
        blanking = true;

        setTimeout(() => {
            currentWidgetSection = category;
            blanking = false;
        }, 50);
    }

    onMount(async () => {
        const widgetId = location.href.split("?widget=")[1];

        const { widgets } = (await Bridge.query("plugins")).data;
        widget = widgets[widgetId];

        console.log(widget);

        widgetSections = widget.settingsLayout?.sections || [];
        currentWidgetSection = widgetSections[0];
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
                {#each widgetSections as widgetSection}
                    {#if widgetSection == currentWidgetSection}
                        <li class="is-active">
                            <a>
                                {widgetSection.name}
                            </a>
                        </li>
                    {:else}
                        <li>
                            <a on:click={switchCategory(widgetSection)}>
                                {widgetSection.name}
                            </a>
                        </li>
                    {/if}
                {/each}
            </ul>
        </div>

        <div class="allow-select has-text-left">
            {#if !blanking}
                {#each currentWidgetSection.items as widgetSettingsOption}
                    <div class="columns">
                        <div class="column" style="max-width: 260px; min-width: 260px;">
                            <span class="has-text-weight-medium">
                                {widgetSettingsOption.name}
                            </span>
                        </div>
                        <div class="column">
                            <FormElement {widget} {widgetSettingsOption} />
                        </div>
                    </div>
                {/each}
            {/if}
        </div>
    </div>
{/if}
