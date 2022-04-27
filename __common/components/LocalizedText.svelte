<script>
    import { onMount, onDestroy } from "svelte";
    import App from "../app.mjs";
    import translate from "../translate.mjs";

    const unregister = [];

    export let opts = {};
    export let key;

    export let slotMapping = [];
    let slotContents = {};

    let language;
    let contentElement;

    function render() {
        if (typeof key != "string") {
            if (contentElement) {
                console.error(
                    "[LocalizedText] Invalid key:",
                    key,
                    contentElement
                );

                contentElement.replaceChildren(
                    `<span style="background: red" title="INVALID TRANSLATION KEY">INVALID</span>`
                );
            }
            return;
        }

        if (language) {
            const { result, usedFallback } = translate(
                language,
                key,
                opts,
                false
            );

            let newContents;

            if (usedFallback) {
                newContents = `<span style="background: red" title="NO TRANSLATION KEY">${result}</span>`;
            } else {
                newContents = result;
            }

            newContents = newContents.split(/(%\w+%)/g);

            for (const [index, value] of newContents.entries()) {
                if (value.startsWith("%")) {
                    const slotName = value.slice(1, -1);
                    const slotId = "item" + slotMapping.indexOf(slotName);

                    newContents[index] = slotContents[slotId];
                } else {
                    const span = document.createElement("span");
                    span.innerHTML = value;
                    newContents[index] = span;
                }
            }

            if (slotMapping.length > 0) {
                console.debug(
                    "[LocalizedText] localized with slots:",
                    key,
                    slotMapping,
                    result,
                    newContents
                );
            }

            contentElement?.replaceChildren(...newContents);
        } else {
            contentElement?.replaceChildren(key);
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

<div style="display: none;">
    <!-- We need 10 of these -->
    <span bind:this={slotContents.item0}><slot name="0" /></span>
    <span bind:this={slotContents.item1}><slot name="1" /></span>
    <span bind:this={slotContents.item2}><slot name="2" /></span>
    <span bind:this={slotContents.item3}><slot name="3" /></span>
    <span bind:this={slotContents.item4}><slot name="4" /></span>
    <span bind:this={slotContents.item5}><slot name="5" /></span>
    <span bind:this={slotContents.item6}><slot name="6" /></span>
    <span bind:this={slotContents.item7}><slot name="7" /></span>
    <span bind:this={slotContents.item8}><slot name="8" /></span>
    <span bind:this={slotContents.item9}><slot name="9" /></span>
</div>

<span bind:this={contentElement} />
