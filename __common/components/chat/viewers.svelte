<script>
    import { createEventDispatcher, onMount } from "svelte";

    import Draggable from "../Draggable.svelte";

    const dispatch = createEventDispatcher();

    let platforms = {};
    let viewersList = [];

    let draggable = null;

    function onUpdate(positionData) {
        dispatch("update", positionData);
    }

    export function getPositionData() {
        return draggable.getPositionData();
    }

    export function setPositionData(x, y, width, height) {
        draggable.setPositionData(x, y, width, height);
    }

    export function onViewersList(e) {
        console.log(e);
        platforms[e.streamer.platform] = e.viewers;

        updateViewersList();
    }

    export function onAuthUpdate(signedInPlatforms) {
        for (const platform of Object.keys(platforms)) {
            if (!signedInPlatforms.includes(platform)) {
                delete platforms[platform];
            }
        }

        updateViewersList();
    }

    function updateViewersList() {
        let list = [];

        for (const viewers of Object.values(platforms)) {
            list.push(...viewers);
        }

        viewersList = list;
    }

    function copyViewersList(e) {
        e.preventDefault();

        const list = [];

        for (const viewer of viewersList) {
            list.push(viewer.displayname);
        }

        Caffeinated.copyText(list.join("\n"), "Copied the viewer list to your clipboard");
    }
</script>

<div class="viewers-list-container" on:contextmenu={copyViewersList} on:dblclick={copyViewersList}>
    <Draggable bind:this={draggable} on:update={onUpdate}>
        <div id="viewers-list">
            <span id="total-count">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-eye" style="transform: translateY(1.5px);">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                </svg>
                {viewersList.length}
            </span>

            <ul class="allow-select">
                {#each viewersList as viewer}
                    <li>
                        {viewer.displayname}
                    </li>
                {/each}
            </ul>
        </div>
    </Draggable>
</div>

<style>
    .viewers-list-container {
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        pointer-events: none;
        z-index: 2000;
        opacity: 0.65;
    }

    #viewers-list {
        position: relative;
        overflow-y: auto;
        overflow-x: hidden;
        height: 100%;
    }

    #total-count {
        position: absolute;
        top: 5px;
        right: 9px;
        text-align: right;
    }
</style>
