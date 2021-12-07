<script>
    import { onMount } from "svelte";

    let maximized = false;
    let focused = true;
    let title = "Casterlabs Caffeinated";
    let icon = "casterlabs";

    function sendMinimize() {
        Bridge.emit("window:minimize");
    }

    function sendMaximize() {
        Bridge.emit("window:maximize");
    }

    function sendClose() {
        Bridge.emit("window:close");
    }

    function parseWindowUpdate(data) {
        maximized = data.maximized;
        focused = data.hasFocus;
        title = data.title;
        icon = data.icon;
    }

    onMount(async () => {
        Bridge.on("window:update", parseWindowUpdate);
        parseWindowUpdate((await Bridge.query("window")).data);
    });
</script>

<!-- Based on: https://codepen.io/agrimsrud/pen/WGgRPP -->

{#if !focused}
    <style>
        :root {
            --title-bar-text-color: var(--title-bar-faded-color) !important;
        }
    </style>
{/if}

<div class="ui-titlebar">
    <div class="ui-titleicon">
        <img src="/img/logo/{icon}.svg" alt="Casterlabs" />
    </div>
    <div class="ui-titletext">
        {title}
    </div>
    <div class="ui-titlecontrols">
        <button class="ui-btn minimize" on:click={sendMinimize}>
            <svg x="0px" y="0px" viewBox="0 0 10.2 1">
                <rect x="0" y="50%" width="10.2" height="1" />
            </svg>
        </button><button class="ui-btn maximize" on:click={sendMaximize}>
            {#if maximized}
                <svg viewBox="0 0 10.2 10.1">
                    <path d="M2.1,0v2H0v8.1h8.2v-2h2V0H2.1z M7.2,9.2H1.1V3h6.1V9.2z M9.2,7.1h-1V2H3.1V1h6.1V7.1z" />
                </svg>
            {:else}
                <svg viewBox="0 0 10 10">
                    <path d="M0,0v10h10V0H0z M9,9H1V1h8V9z" />
                </svg>
            {/if}
        </button><button class="ui-btn close" on:click={sendClose}>
            <svg viewBox="0 0 10 10">
                <polygon points="10.2,0.7 9.5,0 5.1,4.4 0.7,0 0,0.7 4.4,5.1 0,9.5 0.7,10.2 5.1,5.8 9.5,10.2 10.2,9.5 5.8,5.1" />
            </svg>
        </button>
    </div>
</div>

<style>
    .ui-titlebar {
        display: flex;
        width: 100vw;
        height: 32px;
        background: var(--title-bar-color);
        user-select: none;
        cursor: default;
        z-index: 1000;
    }

    .ui-titleicon {
        flex-grow: 0;
        width: 32px;
        height: 32px;
    }

    .ui-titleicon img {
        vertical-align: middle;
        width: 16px;
        height: 100%;
        margin-left: 8px;
    }

    .ui-titletext {
        flex-grow: 2;
        max-height: 32px;
        width: auto;
        font: 12px/32px "Segoe UI", Arial, sans-serif;
        color: var(--title-bar-text-color);
        text-indent: 10px;
        transform: translateX(-12px);
    }

    .ui-titlecontrols {
        max-width: 136px;
        max-height: 32px;
        flex-grow: 1;
    }

    .ui-btn {
        margin: 0;
        padding: 0;
        width: 45px;
        height: 32px;
        border: 0;
        outline: 0;
        background: transparent;
    }

    .ui-btn:hover {
        background: var(--title-bar-highlight);
    }

    .ui-btn.close:hover {
        background: #e81123;
    }

    .ui-btn.close:hover svg {
        filter: invert(1);
    }

    .ui-btn svg path,
    .ui-btn svg rect,
    .ui-btn svg polygon {
        fill: var(--title-bar-text-color);
    }

    .ui-btn svg {
        width: 10px;
        height: 10px;
    }
</style>
