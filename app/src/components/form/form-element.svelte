<script>
    export let module;
    export let itemDeclaration;

    // import FormButton from "./elements/button.svelte";
    import FormCheckbox from "./elements/checkbox.svelte";
    // import FormColor from "./elements/color.svelte";
    // import FormCurrency from "./elements/currency.svelte";
    // import FormDynamic from "./elements/dynamic.svelte";
    // import FormFile from "./elements/file.svelte";
    // import FormFont from "./elements/font.svelte";
    // import FormIframe from "./elements/iframe.svelte";
    // import FormNumber from "./elements/number.svelte";
    // import FormPassword from "./elements/password.svelte";
    // import FormRange from "./elements/range.svelte";
    // import FormSearch from "./elements/search.svelte";
    // import FormSelect from "./elements/select.svelte";
    // import FormText from "./elements/text.svelte";
    // import FormTextArea from "./elements/textarea.svelte";

    import { onMount } from "svelte";

    const ELEMENTS = {
        // "button": FormButton,
        checkbox: FormCheckbox
        // "color": FormColor,
        // "currency": FormCurrency,
        // "dynamic": FormDynamic,
        // "file": FormFile,
        // "font": FormFont,
        // "iframe-src": FormIframe,
        // "number": FormNumber,
        // "password": FormPassword,
        // "range": FormRange,
        // "search": FormSearch,
        // "select": FormSelect,
        // "textarea": FormTextArea,
        // "text": FormText
    };

    let container;

    onMount(async () => {
        const { type } = itemDeclaration;

        const FormElement = ELEMENTS[type];

        if (FormElement) {
            new FormElement({
                target: container,
                props: {
                    module: module,
                    itemDeclaration: itemDeclaration
                }
            });

            console.error(`Unknown element type: ${type}`);
        }
    });
</script>

<span bind:this={container} />
