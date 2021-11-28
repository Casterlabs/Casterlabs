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
    let nameEditorTextContent;

    function editName() {
        Bridge.emit("plugin:rename-widget", { id: widget.id, newName: nameEditorTextContent });
    }

    function deleteWidget() {
        Bridge.emit("plugin:delete-widget", { id: widget.id });
    }

    function fixEditableDiv(elem) {
        elem.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                elem.blur();
            }
        });
    }

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

        nameEditorTextContent = widget.name;
        widgetSections = widget.settingsLayout?.sections || [];
        currentWidgetSection = widgetSections[0];
    });
</script>

{#if widget}
    <div class="has-text-centered">
        <div style="margin-top: 2px; margin-bottom: .5em;">
            <div class="widget-controls">
                <div contenteditable="true" class="title is-5 is-inline-block cursor-edit" bind:textContent={nameEditorTextContent} on:blur={editName} use:fixEditableDiv />

                <div class="buttons is-inline-block are-small">
                    <button on:click={deleteWidget} class="button show-on-hover is-danger is-outlined">
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
                            class="feather feather-trash-2"
                            ><polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /><line
                                x1="10"
                                y1="11"
                                x2="10"
                                y2="17"
                            /><line x1="14" y1="11" x2="14" y2="17" /></svg
                        >
                    </button>
                </div>
            </div>
        </div>

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

        <div class="widget-settings allow-select has-text-left">
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

        <button class="button back-button" onclick="history.back();">
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
                class="feather feather-arrow-left"><line x1="19" y1="12" x2="5" y2="12" /><polyline points="12 19 5 12 12 5" /></svg
            >
        </button>
    </div>
{/if}

<style>
    .widget-settings {
        position: absolute;
        top: 125px;
        bottom: 4.25em;
        left: 3.5em;
        right: 3.5em;
        overflow-x: hidden;
        overflow-y: auto;
    }

    .back-button {
        position: absolute;
        bottom: 2em;
        left: 2em;
    }

    .show-on-hover {
        visibility: hidden;
    }

    .widget-controls:hover .show-on-hover {
        visibility: visible;
    }

    .widget-controls {
        position: relative;
        width: fit-content;
        margin: auto;
        height: 3.5em;
        line-height: 3.5em;
    }

    .widget-controls .title {
        padding-bottom: 0;
    }

    .widget-controls .buttons {
        position: absolute;
        right: -10px;
        transform: translate(100%, 20%);
    }

    .widget-controls .buttons .button {
        margin: auto;
    }
</style>
