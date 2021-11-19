<script>
    export let module;
    export let itemDeclaration;

    export let checked = module.settings[itemDeclaration.name] ?? itemDeclaration.defaultValue;

    import { createEventDispatcher } from "svelte";

    const dispatch = createEventDispatcher();

    function onChange() {
        dispatch("change", {
            checked: checked
        });
    }
</script>

<input class="checkbox" type="checkbox" bind:checked on:change={onChange} />
