<script>
    import { createEventDispatcher, onMount } from "svelte";

    const dispatch = createEventDispatcher();

    let positionData = {
        // These are all in percentages
        x: 0.8,
        y: 0.02,
        width: 0.15,
        height: 0.65
    };

    export function getPositionData() {
        return positionData;
    }

    export function setPositionData(x, y, width, height) {
        positionData = {
            x: x || 0.8,
            y: y || 0.02,
            width: width || 0.15,
            height: height || 0.65
        };
    }

    let resizers = {};
    let contentContainer = null;
    let parentElement = null;

    let resizingLocation = null;
    let resizing = false;
    let dragging = false;

    let lastMouseX = 0;
    let lastMouseY = 0;
    let mouseX = 0;
    let mouseY = 0;

    let debounceId = 0;

    onMount(() => {
        parentElement = contentContainer.parentElement.parentElement;

        for (const [name, element] of Object.entries(resizers)) {
            element.addEventListener("mousedown", (event) => {
                event.preventDefault();
                event.stopPropagation();

                resizingLocation = name;
                resizing = true;
                lastMouseX = event.clientX;
                lastMouseY = event.clientY;
            });
        }

        contentContainer.addEventListener("mousedown", (event) => {
            event.preventDefault();
            event.stopPropagation();
            dragging = true;
            lastMouseX = event.clientX;
            lastMouseY = event.clientY;
        });

        document.addEventListener("mousemove", (e) => {
            // Not for us.
            if (!resizing && !dragging) {
                return;
            }

            const parentWidth = parentElement.offsetWidth;
            const parentHeight = parentElement.offsetHeight;

            if (!mouseX) {
                mouseX = positionData.x * parentWidth;
                mouseY = positionData.y * parentHeight;
            }

            const deltaX = e.clientX - lastMouseX;
            const deltaY = e.clientY - lastMouseY;

            mouseX += deltaX;
            mouseY += deltaY;

            lastMouseX = e.clientX;
            lastMouseY = e.clientY;

            if (resizing) {
                switch (resizingLocation) {
                    case "se-corner": {
                        positionData.height += deltaY / parentHeight;
                        positionData.width += deltaX / parentWidth;
                        break;
                    }

                    case "s-edge": {
                        positionData.height += deltaY / parentHeight;
                        break;
                    }

                    case "e-edge": {
                        positionData.width += deltaX / parentWidth;
                        break;
                    }

                    case "n-edge": {
                        positionData.y += deltaY / parentHeight;
                        positionData.height -= deltaY / parentHeight;
                        break;
                    }

                    case "w-edge": {
                        positionData.x += deltaX / parentWidth;
                        positionData.width -= deltaX / parentWidth;
                        break;
                    }

                    case "sw-corner": {
                        positionData.x += deltaX / parentWidth;
                        positionData.height += deltaY / parentHeight;
                        positionData.width -= deltaX / parentWidth;
                        break;
                    }

                    case "ne-corner": {
                        positionData.y += deltaY / parentHeight;
                        positionData.height -= deltaY / parentHeight;
                        positionData.width += deltaX / parentWidth;
                        break;
                    }

                    case "nw-corner": {
                        positionData.x += deltaX / parentWidth;
                        positionData.y += deltaY / parentHeight;
                        positionData.height -= deltaY / parentHeight;
                        positionData.width -= deltaX / parentWidth;
                        break;
                    }
                }
            } else if (dragging) {
                positionData.x = mouseX / parentWidth;
                positionData.y = mouseY / parentHeight;
            }

            // Debounce logic
            clearTimeout(debounceId);
            debounceId = setTimeout(() => {
                dispatch("update", positionData);
            }, 100);
        });

        document.addEventListener("mouseup", () => {
            resizing = false;
            dragging = false;

            mouseX = null;
            mouseY = null;
        });
    });
</script>

<div
    id="draggable"
    class="box"
    style="
        left: {positionData.x * 100}%; 
        top: {positionData.y * 100}%; 
        width: {positionData.width * 100}%; 
        height: {positionData.height * 100}%;
    "
>
    <div id="nw-corner" bind:this={resizers["nw-corner"]} />
    <div id="n-edge" bind:this={resizers["n-edge"]} />
    <div id="ne-corner" bind:this={resizers["ne-corner"]} />

    <div id="w-edge" bind:this={resizers["w-edge"]} />
    <div id="content" bind:this={contentContainer}><slot /></div>
    <div id="e-edge" bind:this={resizers["e-edge"]} />

    <div id="sw-corner" bind:this={resizers["sw-corner"]} />
    <div id="s-edge" bind:this={resizers["s-edge"]} />
    <div id="se-corner" bind:this={resizers["se-corner"]} />
</div>

<style>
    /* Size and Positioning */
    #draggable {
        --edge-size: 6px;

        position: relative;
        padding: 0px;
        margin: 0px;
        display: flex;
        flex-wrap: wrap;
        justify-content: center;
        pointer-events: all;
    }

    #nw-corner,
    #ne-corner,
    #sw-corner,
    #se-corner {
        width: var(--edge-size);
        height: var(--edge-size);
        /* background-color: red; */
    }

    #n-edge,
    #s-edge {
        width: calc(100% - (var(--edge-size) * 2));
        height: var(--edge-size);
        /* background-color: green; */
    }

    #w-edge,
    #e-edge {
        width: var(--edge-size);
        height: calc(100% - (var(--edge-size) * 2));
        /* background-color: green; */
    }

    #content {
        width: calc(100% - (var(--edge-size) * 2));
        height: calc(100% - (var(--edge-size) * 2));
        position: relative;
        overflow: auto;
        /* background-color: rebeccapurple; */
    }

    /* Cursor */
    #content {
        cursor: move;
    }

    #nw-corner {
        cursor: nw-resize;
    }

    #ne-corner {
        cursor: ne-resize;
    }

    #sw-corner {
        cursor: sw-resize;
    }

    #se-corner {
        cursor: se-resize;
    }

    #n-edge {
        cursor: n-resize;
    }

    #s-edge {
        cursor: s-resize;
    }

    #w-edge {
        cursor: w-resize;
    }

    #e-edge {
        cursor: e-resize;
    }
</style>
