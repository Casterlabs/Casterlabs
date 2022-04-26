<script>
    import { onMount, onDestroy } from "svelte";
    import App from "../app.mjs";
    import translate from "../translate.mjs";

    const unregister = [];

    export let opts = {};
    export let key;

    let language;
    let content;

    function render() {
        if (language) {
            content = translate(language, key, opts);
        } else {
            content = null;
        }
    }

    onMount(() => {
        unregister.push(
            App.on("language", (l) => {
                language = l;
                render();
            })
        );
    });

    onDestroy(() => {
        for (const un of unregister) {
            try {
                App.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

    // Rerender on change
    $: key, render();
    $: opts, render();
</script>

{@html content || key}
