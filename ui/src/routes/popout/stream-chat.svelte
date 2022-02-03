<script>
    import { setPageProperties } from "../__layout.svelte";

    import ChatViewer from "../../components/chat/chat-viewer.svelte";

    import { onMount } from "svelte";

    let viewerElement = {};

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: true
    });

    /* ---------------- */
    /* Bridge Events    */
    /* ---------------- */

    function bridge_processEvent(event) {
        viewerElement.processEvent(event);
    }

    function bridge_onAuthUpdate(data) {
        viewerElement.onAuthUpdate(data);
    }

    function bridge_onChatViewerPreferencesUpdate(data) {
        viewerElement.loadConfig(data);
    }

    /* ---------------- */
    /* Event Handlers   */
    /* ---------------- */

    function onSavePreferences({ detail: data }) {
        // Bridge.emit("ui:save_chat_viewer_preferences", {
        //     preferences: data
        // });
    }

    function onChatSend({ detail: data }) {
        // Bridge.emit("koi:chat_send", data);
    }

    function onModAction({ detail: modAction }) {
        const { type, event } = modAction;
        const platform = event.streamer.platform;

        console.log("[StreamChat]", `onModAction(${type}, ${platform})`);

        function sendCommand(command) {
            // Bridge.emit("koi:chat_send", {
            //     message: command,
            //     platform: platform
            // });
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
                    // Bridge.emit("koi:chat_delete", {
                    //     messageId: event.id,
                    //     platform: platform
                    // });
                }
                return;
            }

            case "upvote": {
                if (platform == "CAFFEINE") {
                    // Bridge.emit("koi:chat_upvote", {
                    //     messageId: event.id,
                    //     platform: platform
                    // });
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

    onMount(async () => {
        document.title = "Stream Chat";

        // // eventHandler.on("auth:update", bridge_onAuthUpdate);
        // // bridge_onAuthUpdate((await Bridge.query("auth")).data);

        // // eventHandler.on("ui:chatViewerPreferences:update", bridge_onChatViewerPreferencesUpdate);
        // // bridge_onChatViewerPreferencesUpdate((await Bridge.query("ui:chatViewerPreferences")).data);

        // Koi.on("*", bridge_processEvent);
        // Koi.eventHistory.forEach(bridge_processEvent);

        // for (const [platform, viewers] of Object.entries(Koi.viewers) {
        //     bridge_processEvent({
        //         streamer: {
        //             platform: platform
        //         },
        //         viewers: viewers,
        //         event_type: "VIEWER_LIST"
        //     });
        // }
    });
</script>

<ChatViewer bind:this={viewerElement} on:chatsend={onChatSend} on:modaction={onModAction} on:savepreferences={onSavePreferences} />
