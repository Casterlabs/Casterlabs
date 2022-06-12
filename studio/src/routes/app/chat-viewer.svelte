<script>
    import { onDestroy, onMount } from "svelte";
    import { goto } from "$app/navigation";

    import { Koi, isKoiLoggedIn, signout } from "./__layout.svelte";

    import ChatViewer from "$lib/components/chat/chat-viewer.svelte";

    let koiEventHandler = Koi.createThrowawayEventHandler();
    let viewerElement = {};

    /* ---------------- */
    /* Event Handlers   */
    /* ---------------- */

    function onSavePreferences({ detail: data }) {
        localStorage.setItem("cl_studio:chat_viewer:preferences", JSON.stringify(data));
    }

    function onChatSend({ detail: data }) {
        Koi.sendMessage(data.message);
    }

    function onModAction({ detail: modAction }) {
        const { type, event } = modAction;
        const platform = event.streamer.platform;

        console.log("[StreamChat]", `onModAction(${type}, ${platform})`);

        switch (type) {
            case "ban": {
                switch (platform) {
                    case "TWITCH": {
                        Koi.sendMessage(`/ban ${event.sender.username}`);
                        return;
                    }

                    case "TROVO": {
                        Koi.sendMessage(`/ban ${event.sender.username}`);
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
                        Koi.sendMessage(`/timeout ${event.sender.username} 600`);
                        return;
                    }

                    case "TROVO": {
                        Koi.sendMessage(`/ban ${event.sender.username} 600`);
                        return;
                    }

                    default: {
                        return;
                    }
                }
            }

            case "delete": {
                Koi.deleteMessage(event.id);
                return;
            }

            case "upvote": {
                Koi.upvoteMessage(event.id);
                return;
            }

            case "raid": {
                switch (platform) {
                    case "TWITCH": {
                        Koi.sendMessage(`/raid ${event.sender.username}`);
                        return;
                    }

                    case "CAFFEINE": {
                        Koi.sendMessage(`/afterparty ${event.sender.username}`);
                        return;
                    }

                    case "TROVO": {
                        Koi.sendMessage(`/host ${event.sender.username}`);
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
        if (!isKoiLoggedIn()) {
            goto("/app");
            return;
        }

        {
            const prefs = localStorage.getItem("cl_studio:chat_viewer:preferences");
            if (prefs) {
                viewerElement.loadConfig(JSON.parse(prefs));
            }
        }

        viewerElement.onAuthUpdate([Koi.userData.streamer.platform]);

        koiEventHandler.on("*", (type, event) => {
            viewerElement.processEvent(event);
        });
    });

    onDestroy(() => {
        koiEventHandler.destroy();
    });
</script>

<section class="main-layout">
    <div class="chat-container">
        <ChatViewer bind:this={viewerElement} on:chatsend={onChatSend} on:modaction={onModAction} on:savepreferences={onSavePreferences} />
    </div>
    <a id="signout-button" on:click={signout}>
        <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="white"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="feather feather-log-out"
            style="filter: invert(var(--white-invert-factor));"
        >
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
        </svg>
    </a>
</section>

<style>
    .main-layout {
        height: 100vh;
        width: 100vw;
    }

    .chat-container {
        width: 100%;
        height: 100%;
    }

    #signout-button {
        position: absolute;
        top: 1em;
        right: 1em;
        z-index: 130;
    }
</style>
