<script>
    export let module;
    export let itemDeclaration;

    let checked = module.settings[itemDeclaration.name] ?? itemDeclaration.defaultValue;
</script>

<input class="checkbox" type="checkbox" bind:checked="{checked}" />