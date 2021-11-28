<script>
    export let widget;
    export let widgetSettingsOption;

    export let value = widget.settings[widgetSettingsOption.id] ?? widgetSettingsOption.extraData.defaultValue;

    import { createEventDispatcher } from "svelte";

    const dispatch = createEventDispatcher();

    function onChange() {
        dispatch("change", {
            value: value
        });
    }

    function onInput() {
        dispatch("value", {
            value: value
        });
    }
</script>

<input class="input" type="password" bind:value on:change={onChange} on:input={onInput} placeholder={widgetSettingsOption.extraData.placeholder} />
