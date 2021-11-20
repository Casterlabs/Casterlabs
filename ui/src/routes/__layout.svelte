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

    function updateTheme({ theme }) {
        const htmlElement = document.querySelector("html");

        switch (theme) {
            case "light": {
                htmlElement.classList.remove("bulma-dark-mode");
                return;
            }

            case "dark":
            default: {
                htmlElement.classList.add("bulma-dark-mode");
                return;
            }
        }
    }

    onMount(async () => {
        window.goto = goto;
        feather.replace();

        Bridge.on("goto", ({ path }) => goto(path));

        Bridge.on("pref-update:ui", updateTheme);
        updateTheme((await Bridge.query("ui")).data);
    });
</script>

<!-- Titlebar -->
<!-- svelte-ignore a11y-missing-attribute -->
<!-- <section class="title-bar no-select">
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
    <div class="title-actions hide">
        <span class="title is-6">
            <a class="minimize title-action">
                <i data-feather="minus" />
            </a><a class="maximize title-action">
                <i data-feather="maximize" />
            </a><a class="close title-action">
                <i data-feather="x" />
            </a>
        </span>
        <script>
            document.querySelector(".minimize").addEventListener("click", () => {
                window.Bridge.emit("window:minimize");
            });

            document.querySelector(".maximize").addEventListener("click", () => {
                window.Bridge.emit("window:minmax");
            });

            document.querySelector(".close").addEventListener("click", () => {
                window.Bridge.emit("window:close");
            });
        </script>
    </div>
</section> -->

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
        top: 0;
        bottom: 0;
        left: 0;
        width: var(--side-bar-width);
    }

    .svelte-container {
        position: absolute;
        top: 0;
        bottom: 0;
        left: var(--side-bar-width);
        right: 0;
        overflow-x: hidden;
        overflow-y: auto;
    }

    /* #svelte {
        min-height: 100%;
    } */
</style>
