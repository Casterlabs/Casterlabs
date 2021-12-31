<script>
    export let widget;
    export let widgetSettingsSection;
    export let widgetSettingsOption;

    import FormCheckbox from "./elements/checkbox.svelte";
    import FormColor from "./elements/color.svelte";
    import FormNumber from "./elements/number.svelte";
    import FormSelect from "./elements/select.svelte";
    import FormText from "./elements/text.svelte";
    import FormTextArea from "./elements/textarea.svelte";
    import FormPassword from "./elements/password.svelte";
    import FormCurrency from "./elements/currency.svelte";
    import FormFont from "./elements/font.svelte";
    // import FormDynamic from "./elements/dynamic.svelte";
    // import FormFile from "./elements/file.svelte";
    // import FormRange from "./elements/range.svelte";
    // import FormSearch from "./elements/search.svelte";
    // import FormButton from "./elements/button.svelte";
    // import FormIframe from "./elements/iframe.svelte";

    const type = widgetSettingsOption.type.toLowerCase();
    const settingsKey = `${widgetSettingsSection.id}.${widgetSettingsOption.id}`;

    let value = widget.settings[settingsKey];
    let inputDebounce = -1;

    if (value == undefined || value == null) {
        value = widgetSettingsOption.extraData.defaultValue;
    }

    // console.debug("[FormElement]", settingsKey, value);

    function onInput() {
        if (inputDebounce == -1) {
            inputDebounce = setTimeout(onChange, 275);
        }
    }

    async function onChange() {
        clearTimeout(inputDebounce);
        inputDebounce = -1;

        Bridge.emit("plugins:edit-widget-settings", {
            id: widget.id,
            key: settingsKey,
            newValue: value
        });
    }
</script>

<span>
    {#if type === "html"}
        {@html widgetSettingsOption.extraData.html}
    {:else if type === "checkbox"}
        <FormCheckbox {widgetSettingsOption} bind:value on:input={onInput} on:change={onChange} />
    {:else if type === "color"}
        <FormColor {widgetSettingsOption} bind:value on:input={onInput} on:change={onChange} />
    {:else if type === "number"}
        <FormNumber {widgetSettingsOption} bind:value on:input={onInput} on:change={onChange} />
    {:else if type === "dropdown"}
        <FormSelect {widgetSettingsOption} bind:value on:input={onInput} on:change={onChange} />
    {:else if type === "text"}
        <FormText {widgetSettingsOption} bind:value on:input={onInput} on:change={onChange} />
    {:else if type === "textarea"}
        <FormTextArea {widgetSettingsOption} bind:value on:input={onInput} on:change={onChange} />
    {:else if type === "password"}
        <FormPassword {widgetSettingsOption} bind:value on:input={onInput} on:change={onChange} />
    {:else if type === "currency"}
        <FormCurrency {widgetSettingsOption} bind:value on:input={onInput} on:change={onChange} />
    {:else if type === "font"}
        <FormFont {widgetSettingsOption} bind:value on:input={onInput} on:change={onChange} />
    {:else}
        ... {type}
    {/if}
    <!-- {settingsKey} -->
</span>
