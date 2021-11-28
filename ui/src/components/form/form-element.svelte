<script>
    export let widget;
    export let widgetSettingsOption;

    import FormCheckbox from "./elements/checkbox.svelte";
    import FormColor from "./elements/color.svelte";
    import FormNumber from "./elements/number.svelte";
    import FormSelect from "./elements/select.svelte";
    import FormText from "./elements/text.svelte";
    import FormTextArea from "./elements/textarea.svelte";
    import FormPassword from "./elements/password.svelte";
    // import FormCurrency from "./elements/currency.svelte";
    // import FormFont from "./elements/font.svelte";
    // import FormDynamic from "./elements/dynamic.svelte";
    // import FormFile from "./elements/file.svelte";
    // import FormRange from "./elements/range.svelte";
    // import FormSearch from "./elements/search.svelte";
    // import FormButton from "./elements/button.svelte";
    // import FormIframe from "./elements/iframe.svelte";

    const type = widgetSettingsOption.type.toLowerCase();
</script>

<span>
    {#if type === "checkbox"}
        <FormCheckbox {widget} {widgetSettingsOption} />
    {:else if type === "color"}
        <FormColor {widget} {widgetSettingsOption} />
    {:else if type === "number"}
        <FormNumber {widget} {widgetSettingsOption} />
    {:else if type === "dropdown"}
        <FormSelect {widget} {widgetSettingsOption} />
    {:else if type === "text"}
        <FormText {widget} {widgetSettingsOption} />
    {:else if type === "textarea"}
        <FormTextArea {widget} {widgetSettingsOption} />
    {:else if type === "password"}
        <FormPassword {widget} {widgetSettingsOption} />
    {:else}
        ... {type}
    {/if}
</span>
