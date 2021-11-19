<script>
    export let module;
    export let itemDeclaration;

    export let value = module.settings[itemDeclaration.name] ?? itemDeclaration.defaultValue;

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

<textarea class="textarea" bind:value on:change={onChange} on:input={onInput} placeholder={itemDeclaration.placeholder} rows={itemDeclaration.rows || 4} />
