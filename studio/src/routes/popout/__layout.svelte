<script>
    import { onMount } from "svelte";

    import LoadingSpinner from "$lib/components/LoadingSpinner.svelte";

    let isConnected = false;

    /* ---------------- */
    /* Life Cycle   */
    /* ---------------- */

    onMount(async () => {
        document.title = "Casterlabs Caffeinated - Popout";

        if (!window.__common) {
            window.__common = await import("$lib/__common.mjs");
        }

        const conn = await import("https://widgets.casterlabs.co/caffeinated/widget.mjs");

        function initHandler() {
            isConnected = true;
            return true;
        }

        function disconnectHandler() {
            setTimeout(() => {
                conn.init({ initHandler, disconnectHandler });
            }, 7500);
            isConnected = false;
            return false;
        }

        conn.init({ initHandler, disconnectHandler });

        // Intercept all anchors and send the open via Caffeinated
        new MutationObserver(() => {
            for (const anchor of document.querySelectorAll("a")) {
                // Intercept all rel="external" and target="_blank" anchors and add a click listener.
                if ((anchor.rel == "external" || anchor.target == "_blank") && !anchor.getAttribute("dest")) {
                    anchor.setAttribute("dest", anchor.href);
                    anchor.href = "#"; // Clear the property incase it tries to open the link.

                    // On click we prevent the navigation and openLink with the href.
                    // We use the onclick function rather than
                    // addEventListener("click", ...) because it helps prevent duplicates.
                    anchor.onclick = () => {
                        Widget.emit("openLink", anchor.getAttribute("dest"));
                        return false;
                    };
                }
            }
        }).observe(document.body, {
            subtree: true,
            childList: true
        });

        // AppContext.on("theme-update", (theme) => {
        //     if (theme.id == "co.casterlabs.dark") {
        document.documentElement.classList.add("bulma-dark-mode");
        //     } else {
        //         document.documentElement.classList.remove("bulma-dark-mode");
        //     }
        // });
        // AppContext.broadcast("theme-update", AppContext.currentTheme);
    });
</script>

{#if isConnected}
    <slot />
{:else}
    <div class="loading-spinner">
        <LoadingSpinner />
    </div>
{/if}

<style>
    .loading-spinner {
        margin: auto;
        margin-top: 125px;
        width: 50px;
        height: 50px;
    }
</style>
