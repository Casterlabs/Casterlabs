<script>
    export let module;
    export let itemDeclaration;

    // import FormButton from "./elements/button.svelte";
    import FormCheckbox from "./elements/checkbox.svelte";
    import FormColor from "./elements/color.svelte";
    // import FormCurrency from "./elements/currency.svelte";
    // import FormDynamic from "./elements/dynamic.svelte";
    // import FormFile from "./elements/file.svelte";
    // import FormFont from "./elements/font.svelte";
    // import FormIframe from "./elements/iframe.svelte";
    import FormNumber from "./elements/number.svelte";
    // import FormPassword from "./elements/password.svelte";
    // import FormRange from "./elements/range.svelte";
    // import FormSearch from "./elements/search.svelte";
    import FormSelect from "./elements/select.svelte";
    // import FormText from "./elements/text.svelte";
    // import FormTextArea from "./elements/textarea.svelte";

    const { type } = itemDeclaration;
</script>

<span>
    {#if type === "checkbox"}
        <FormCheckbox {module} {itemDeclaration} />
    {:else if type === "color"}
        <FormColor {module} {itemDeclaration} />
    {:else if type === "number"}
        <FormNumber {module} {itemDeclaration} />
    {:else if type === "select"}
        <FormSelect {module} {itemDeclaration} />
    {:else}
        ... {type}
    {/if}
</span>
