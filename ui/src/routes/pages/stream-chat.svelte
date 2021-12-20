<script>
    import { setPageProperties } from "../__layout.svelte";

    import ChatViewer from "../../components/chat/chat-viewer.svelte";

    import { onMount, onDestroy } from "svelte";
    import { bind } from "svelte/internal";

    let eventHandler;
    let viewerElement = {};

    setPageProperties({
        showSideBar: true,
        pageTitle: "",
        allowNavigateBackwards: true
    });

    function processEvent(event) {
        viewerElement.processEvent(event);
    }

    function onAuthUpdate(data) {
        viewerElement.onAuthUpdate(data);
    }

    function onChatSend({ detail }) {
        Bridge.emit("koi:chatsend", detail);
    }

    onDestroy(() => {
        eventHandler?.destroy();
    });

    onMount(async () => {
        eventHandler = Bridge.createThrowawayEventHandler();

        eventHandler.on("auth:update", onAuthUpdate);
        onAuthUpdate((await Bridge.query("auth")).data);

        eventHandler.on("koi:event", processEvent);
        (await Bridge.query("koi:history")).data.forEach(processEvent);
    });
</script>

<ChatViewer bind:this={viewerElement} on:chatsend={onChatSend} />
