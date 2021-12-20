<script>
    import { setPageProperties } from "../__layout.svelte";

    import ChatViewer from "../../components/chat/chat-viewer.svelte";

    import { onMount, onDestroy } from "svelte";

    let eventHandler;
    let viewerElement = {};

    setPageProperties({
        showSideBar: true,
        pageTitle: "",
        allowNavigateBackwards: true
    });

    /* ---------------- */
    /* Bridge Events    */
    /* ---------------- */

    function processEvent(event) {
        viewerElement.processEvent(event);
    }

    function onAuthUpdate(data) {
        viewerElement.onAuthUpdate(data);
    }

    /* ---------------- */
    /* Event Handlers   */
    /* ---------------- */

    function onChatSend({ detail: data }) {
        Bridge.emit("koi:chat_send", data);
    }

    function onModAction({ detail: modAction }) {
        const { type, event } = modAction;
        const platform = event.streamer.platform;

        console.log("[StreamChat]", `onModAction(${type}, ${platform})`);

        function sendCommand(command) {
            Bridge.emit("koi:chat_send", {
                message: command,
                platform: platform
            });
        }

        switch (type) {
            case "ban": {
                switch (platform) {
                    case "TWITCH": {
                        sendCommand(`/ban ${event.sender.username}`);
                        return;
                    }

                    case "TROVO": {
                        sendCommand(`/ban ${event.sender.username}`);
                        return;
                    }

                    default: {
                        return;
                    }
                }
            }

            case "timeout": {
                // We timeout for 10 minutes
                switch (platform) {
                    case "TWITCH": {
                        sendCommand(`/timeout ${event.sender.username} 600`);
                        return;
                    }

                    case "TROVO": {
                        sendCommand(`/ban ${event.sender.username} 600`);
                        return;
                    }

                    default: {
                        return;
                    }
                }
            }

            case "delete": {
                if (["TWITCH", "BRIME", "TROVO"].includes(platform)) {
                    Bridge.emit("koi:chat_delete", {
                        messageId: event.id,
                        platform: platform
                    });
                }
                return;
            }

            case "upvote": {
                if (platform == "CAFFEINE") {
                    Bridge.emit("koi:chat_upvote", {
                        messageId: event.id,
                        platform: platform
                    });
                }
                return;
            }

            case "raid": {
                switch (platform) {
                    case "TWITCH": {
                        sendCommand(`/raid ${event.sender.username}`);
                        return;
                    }

                    case "CAFFEINE": {
                        sendCommand(`/afterparty ${event.sender.username}`);
                        return;
                    }

                    case "TROVO": {
                        sendCommand(`/host ${event.sender.username}`);
                        return;
                    }

                    default: {
                        return;
                    }
                }
            }
        }
    }

    /* ---------------- */
    /* Life Cycle   */
    /* ---------------- */

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

<ChatViewer bind:this={viewerElement} on:chatsend={onChatSend} on:modaction={onModAction} />
