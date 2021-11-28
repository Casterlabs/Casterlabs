<script>
    export let widget;
    export let widgetSettingsOption;

    export let checked = widget.settings[widgetSettingsOption.id] ?? widgetSettingsOption.extraData.defaultValue;

    import { createEventDispatcher } from "svelte";

    const dispatch = createEventDispatcher();

    function onChange() {
        dispatch("change", {
            checked: checked
        });
    }
</script>

<input class="checkbox" type="checkbox" bind:checked on:change={onChange} />
