<script>
    import LoadingSpinner from "$lib/components/LoadingSpinner.svelte";

    import { onMount } from "svelte";

    let isConnected = false;

    import ChatViewer from "$lib/components/chat/chat-viewer.svelte";

    let viewerElement = {};

    /* ---------------- */
    /* Widget Events    */
    /* ---------------- */

    function bridge_processEvent(event) {
        viewerElement.processEvent(event);
    }

    function bridge_onAuthUpdate(data) {
        viewerElement.onAuthUpdate(data);
    }

    /* ---------------- */
    /* Event Handlers   */
    /* ---------------- */

    function onSavePreferences({ detail: data }) {
        localStorage.setItem("cl_studio:chat_viewer:preferences", JSON.stringify(data));
    }

    function onChatSend({ detail: data }) {
        Widget.emit("koi:chat_send", data);
    }

    function onModAction({ detail: modAction }) {
        const { type, event } = modAction;
        const platform = event.streamer.platform;

        console.log("[StreamChat]", `onModAction(${type}, ${platform})`);

        function sendCommand(command) {
            Widget.emit("koi:chat_send", {
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
                    Widget.emit("koi:chat_delete", {
                        messageId: event.id,
                        platform: platform
                    });
                }
                return;
            }

            case "upvote": {
                if (platform == "CAFFEINE") {
                    Widget.emit("koi:chat_upvote", {
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

    function initViewer() {
        {
            const prefs = localStorage.getItem("cl_studio:chat_viewer:preferences");
            if (prefs) {
                viewerElement.loadConfig(JSON.parse(prefs));
            }
        }

        Widget.on("auth:update", bridge_onAuthUpdate);
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

        bridge_onAuthUpdate({ koiAuth: Koi.userStates });
    }

    onMount(async () => {
        document.title = "Casterlabs-Caffeinated - Stream Chat";

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
    });
</script>

{#if isConnected}
    <ChatViewer bind:this={viewerElement} on:chatsend={onChatSend} on:modaction={onModAction} on:savepreferences={onSavePreferences} on:mount={initViewer} />
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
