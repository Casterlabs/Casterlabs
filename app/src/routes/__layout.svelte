<script context="module">
    let elementUpdateHandler = null;

    let notifications = [];

    let pageAttributes = {
        showSideBar: false,
        pageTitle: "",
        allowNavigateBackwards: false
    };

    function addNotification() {
        // TODO
    }

    function setPageProperties(val) {
        pageAttributes = {
            showSideBar: false,
            pageTitle: "",
            allowNavigateBackwards: false,

            // Spread the `val` so we have these as defaults^
            ...val
        };

        if (elementUpdateHandler) {
            elementUpdateHandler(pageAttributes);
        }

        if (typeof window != "undefined") {
            window.currentPageAttributes = pageAttributes;
        }

        if (typeof document != "undefined") {
            if (pageAttributes.pageTitle && pageAttributes.pageTitle.length > 0) {
                document.title = `Casterlabs Caffeinated - ${pageAttributes.pageTitle}`;
            } else {
                document.title = "Casterlabs Caffeinated";
            }
        }
    }

    export { addNotification, setPageProperties };
</script>

<script>
    import SideBar from "../components/side-bar.svelte";

    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    elementUpdateHandler = update;

    let currentPageAttributes = pageAttributes;

    function update(val) {
        currentPageAttributes = val;
    }

    onMount(() => {
        window.goto = goto;
        window.feather.replace();
    });
</script>

<!-- Titlebar -->
<section class="title-bar no-select">
    <div class=" title-bar-title">
        <img class="app-logo" src="/img/logo/casterlabs.png" alt="Casterlabs" />
        <span class="current-page title is-6">
            {#if currentPageAttributes.pageTitle.length > 0}
                Caffeinated - {currentPageAttributes.pageTitle}
            {:else}
                Caffeinated
            {/if}
        </span>
    </div>
    <div class="title-actions">
        <span class="title is-6">
            <!-- svelte-ignore a11y-missing-attribute -->
            <a class="minimize title-action">
                <i data-feather="minus" />
            </a><!-- svelte-ignore a11y-missing-attribute --><a class="maximize title-action">
                <i data-feather="maximize" />
            </a><!-- svelte-ignore a11y-missing-attribute --><a class="close title-action">
                <i data-feather="x" />
            </a>
        </span>
        <script>
            document.querySelector(".minimize").addEventListener("click", () => {
                currentWindow.minimize();
            });

            document.querySelector(".maximize").addEventListener("click", () => {
                currentWindow.isMaximized() ? currentWindow.unmaximize() : currentWindow.maximize();
            });

            document.querySelector(".close").addEventListener("click", () => {
                app.exit();
            });
        </script>
    </div>
</section>

<section id="notifications" class="no-select" />

<section id="body-content" class="no-select">
    {#if currentPageAttributes.showSideBar}
        <div id="side-bar">
            <SideBar />
        </div>
        <style>
            :root {
                --side-bar-width: 200px;
            }
        </style>
    {/if}

    <div class="svelte-container">
        <div id="svelte">
            <slot />
        </div>
    </div>
</section>

<style>
    :global(#side-bar) {
        position: absolute;
        top: var(--title-bar-height);
        bottom: 0;
        left: 0;
        width: var(--side-bar-width);
    }

    .svelte-container {
        position: absolute;
        top: var(--title-bar-height);
        bottom: 0;
        left: var(--side-bar-width);
        right: 0;
        overflow-x: hidden;
        overflow-y: auto;
    }

    #svelte {
        min-height: 100%;
    }
</style>
