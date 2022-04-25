<script>
    import { onMount, onDestroy } from "svelte";
    import translate from "../translate.mjs";

    const unregister = [];

    export let opts = {};
    export let key;

    let language = "en-US";
    let content;

    function render() {
        if (language) {
            content = translate(language, key, opts);
        } else {
            content = null;
        }
    }

    onMount(() => {
        if (window.Caffeinated) {
            unregister.push(
                Caffeinated.UI.mutate("preferences", (preferences) => {
                    // console.log(preferences);
                    language = preferences.language;
                })
            );
        }
    });

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

    // Rerender on change
    $: language, render();
    $: key, render();
    $: opts, render();
</script>

{@html content}
