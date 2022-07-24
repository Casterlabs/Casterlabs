<script>
    import ChatViewer from "$lib/components/chat/chat-viewer.svelte";

    let viewerElement = {};

    /* ---------------- */
    /* Widget Events    */
    /* ---------------- */

    function bridge_processEvent(event) {
        viewerElement.processEvent(event);
    }

    function bridge_onAuthUpdate(platforms) {
        viewerElement.onAuthUpdate(platforms);
    }

    /* ---------------- */
    /* Event Handlers   */
    /* ---------------- */

    function onSavePreferences({ detail: data }) {
        Widget.emit("savePreferences", data);
    }

    function onChatSend({ detail: data }) {
        const { platform, message, replyTarget } = data;
        Koi.sendChat(platform, message, "CLIENT", replyTarget, false);
    }

    function onModAction({ detail: modAction }) {
        const { type, event } = modAction;
        const platform = event.streamer.platform;

        console.log("[StreamChat]", `onModAction(${type}, ${platform})`);

        function sendCommand(command) {
            Koi.sendChat(platform, command);
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
                    Koi.deleteChat(platform, event.id);
                }
                return;
            }

            case "upvote": {
                if (platform == "CAFFEINE") {
                    Koi.upvoteChat(platform, event.id);
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

    function initViewer() {
        viewerElement.loadConfig(Widget.getSetting("preferences") || {});

        Widget.on("update", () => {
            viewerElement.loadConfig(Widget.getSetting("preferences") || {});
        });

        Widget.on("auth:update", ({ koiAuth }) => bridge_onAuthUpdate(Object.keys(koiAuth)));
        Widget.on("__eval", eval);

        Koi.on("*", (t, event) => bridge_processEvent(event));
        Koi.eventHistory.forEach(bridge_processEvent);

        for (const [platform, viewers] of Object.entries(Koi.viewers)) {
            bridge_processEvent({
                streamer: {
                    platform: platform
                },
                viewers: viewers,
                event_type: "VIEWER_LIST"
            });
        }

        bridge_onAuthUpdate(Object.keys(Koi.userStates));
    }
</script>

<ChatViewer bind:this={viewerElement} on:chatsend={onChatSend} on:modaction={onModAction} on:savepreferences={onSavePreferences} on:mount={initViewer} />
