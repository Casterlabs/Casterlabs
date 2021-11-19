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
</script>

<div class="select">
    <select bind:value on:blur={onChange}>
        {#each Object.entries(itemDeclaration.options) as option}
            <option value={option[0]}>{option[1]}</option>
        {/each}
    </select>
</div>

<style>
    .select {
        height: 1em;
    }

    .select::after {
        border-width: 2.5px !important;
        top: 100% !important;
        right: 15px !important;
        width: 0.5em !important;
        height: 0.5em !important;
    }

    select {
        font-size: 0.75em;
    }
</style>
