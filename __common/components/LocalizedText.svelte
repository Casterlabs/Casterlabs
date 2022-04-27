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
            const { result, usedFallback } = translate(
                language,
                key,
                opts,
                false
            );

            if (usedFallback) {
                content = `<span style="background: red" title="NO TRANSLATION KEY">${result}</span>`;
            } else {
                content = result;
            }
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
