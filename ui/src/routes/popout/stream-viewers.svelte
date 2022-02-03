<script>
    import { setPageProperties } from "../__layout.svelte";

    import { onMount, onDestroy } from "svelte";

    let eventHandler;

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: true
    });

    /* ---------------- */
    /* Life Cycle   */
    /* ---------------- */

    onDestroy(() => {
        eventHandler?.destroy();
    });

    onMount(async () => {
        document.title = "Viewers";

        eventHandler = Bridge.createThrowawayEventHandler();

        eventHandler.on("auth:update", bridge_onAuthUpdate);
        bridge_onAuthUpdate((await Bridge.query("auth")).data);

        eventHandler.on("koi:event", bridge_processEvent);
        (await Bridge.query("koi:history")).data.forEach(bridge_processEvent);

        for (const [platform, viewers] of Object.entries((await Bridge.query("koi:viewers")).data)) {
            bridge_processEvent({
                streamer: {
                    platform: platform
                },
                viewers: viewers,
                event_type: "VIEWER_LIST"
            });
        }
    });
</script>

// TODO :^)
