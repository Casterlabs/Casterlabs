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
            document.title = pageAttributes.pageTitle || "";
        }
    }

    export { addNotification, setPageProperties };
</script>

<script>
    import SideBar from "../components/side-bar.svelte";
    import WindowsTitleBar from "../components/titlebar/windows.svelte";

    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    let showTitleBar = false;

    elementUpdateHandler = update;

    let currentPageAttributes = pageAttributes;

    function update(val) {
        currentPageAttributes = val;
    }

    function updateTheme(theme) {
        let html = [];

        for (const css of theme.css) {
            if (theme.isInlineCss) {
                html.push(`<style>${css}</style>`);
            } else {
                html.push(`<link rel="stylesheet" href="${css}" />`);
            }
        }

        html = html.join("");

        const documentElement = document.documentElement;

        console.info("[__layout__] Updated theme:", theme, "\n", html);
        documentElement.classList = theme.classes;
        document.querySelector("#styles").innerHTML = html;

        if (theme.isDark) {
            documentElement.classList.add("app-is-dark");
        } else {
            documentElement.classList.add("app-is-light");
        }
    }

    function parseWindowUpdate(data) {
        // console.log("[__layout]", "Window state data: ", data);

        // Incase it's needed elsewhere.
        document.title = data.title;

        if (data.platform == "WINDOWS" && data.enableTitleBar) {
            showTitleBar = true;
        }
    }

    onMount(async () => {
        if (!Bridge) {
            alert("BRIDGE NOT INSTALLED!");
        }

        window.goto = goto;

        Bridge.on("goto", ({ path }) => goto(path));

        Bridge.on("window:update", parseWindowUpdate);
        parseWindowUpdate((await Bridge.query("window")).data);

        Bridge.on("theme:update", updateTheme);
        updateTheme((await Bridge.query("theme")).data);
    });
</script>

<!-- Titlebar -->
{#if showTitleBar}
    <section class="title-bar no-select">
        <WindowsTitleBar />
    </section>
    <style>
        :root {
            --title-bar-height: 32px !important;
        }
    </style>
{/if}

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
    :root {
        --title-bar-height: 0px;
    }

    #side-bar {
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

    /* #svelte {
        min-height: 100%;
    } */
</style>
