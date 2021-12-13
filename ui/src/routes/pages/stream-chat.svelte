<script>
    import { setPageProperties } from "../__layout.svelte";

    import ChatMessage from "../../components/chat/chat-message.svelte";

    import { onMount, onDestroy } from "svelte";

    const DISPLAYABLE_EVENTS = ["FOLLOW", "CHAT", "DONATION", "SUBSCRIPTION", "VIEWER_JOIN", "VIEWER_LEAVE", "RAID", "CHANNEL_POINTS", "CLEARCHAT"];

    let eventHandler;

    let chatHistory = {};
    let isMultiPlatform = true;
    let chatbox;

    let chatSettingsOpen = false;

    setPageProperties({
        showSideBar: true,
        pageTitle: "",
        allowNavigateBackwards: true
    });

    function onMeta(event) {
        const chatElement = chatHistory[event.id];

        chatElement.upvotes = event.upvotes;
        chatElement.isDeleted = !event.is_visible;
    }

    // TODO better processing of messages, this works for now.
    function processEvent(event) {
        if (event.event_type == "META") {
            onMeta(event);
        }

        // This means that we must delete ALL of the messages from a user.
        else if (event.event_type == "CLEARCHAT" && event.user_upid) {
            for (const chatMessage of Object.values(chatHistory)) {
                const koiEvent = chatMessage.koiEvent;

                if (koiEvent.sender && koiEvent.sender.UPID == event.user_upid) {
                    chatMessage.isDeleted = true;
                }
            }
        }

        // Display the chat message.
        else if (DISPLAYABLE_EVENTS.includes(event.event_type)) {
            // Mark old messages as deleted.
            // We do still need to create the message to let the user know chat was cleared.
            if (event.event_type == "CLEARCHAT") {
                const now = Date.now();

                for (const chatMessage of Object.values(chatHistory)) {
                    if (chatMessage.timestamp < now) {
                        chatMessage.isDeleted = true;
                    }
                }
            }

            const elem = document.createElement("li");

            const message = new ChatMessage({
                target: elem,
                props: {
                    koiEvent: event,
                    isMultiPlatform: isMultiPlatform
                }
            });

            if (event.id) {
                // This event is editable in some way, shape, or form.
                // (so, we must keep track of it)
                chatHistory[event.id] = message;
            }

            chatbox.appendChild(elem);
        } else {
            return;
        }

        console.log("[StreamChat]", "Processed event:", event);
    }

    function toggleChatSettings() {
        chatSettingsOpen = !chatSettingsOpen;
    }

    onDestroy(() => {
        eventHandler?.destroy();
    });

    onMount(async () => {
        eventHandler = Bridge.createThrowawayEventHandler();

        window.test = chatHistory;

        eventHandler.on("koi:event", processEvent);
        (await Bridge.query("koi:history")).data.forEach(processEvent);
    });
</script>

<div class="stream-chat-container {chatSettingsOpen ? 'chat-settings-open' : ''}">
    <div id="chat-box" class="allow-select">
        <ul bind:this={chatbox} />
    </div>

    <div id="chat-settings" class="box">some settings here.</div>

    <div id="interact-box">
        <div class="field has-addons">
            <div class="control is-expanded" style="position: relative;">
                <input class="input" type="text" placeholder="Send a message" />

                <!-- svelte-ignore a11y-missing-attribute -->
                <a class="chat-settings-button highlight-on-hover" on:click={toggleChatSettings}>
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="white"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        class="feather feather-settings"
                    >
                        <circle cx="12" cy="12" r="3" />
                        <path
                            d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"
                        />
                    </svg>
                </a>
            </div>
            <div class="control">
                <button class="button"> Send </button>
            </div>
        </div>
    </div>
</div>

<style>
    :root {
        --interact-height: 40.5px;
        --interact-top-margin: 12px;
        --interact-bottom-margin: 25px;
    }

    .stream-chat-container {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
    }

    #chat-box {
        position: absolute;
        top: 0;
        bottom: calc(var(--interact-top-margin) + var(--interact-height) + var(--interact-bottom-margin));
        left: 0;
        right: 0;
        font-size: 1.05em;
        padding-top: 10px;
        overflow-y: auto;
        overflow-x: hidden;
    }

    #interact-box {
        position: absolute;
        bottom: var(--interact-bottom-margin);
        left: 15px;
        right: 15px;
        height: var(--interact-height);
    }

    /* Chat settings */

    .chat-settings-button {
        position: absolute;
        top: 5px;
        right: 10px;
        z-index: 20;
        width: 30px;
        height: 30px;
        transition: 0.15s;
        border-radius: 5px;
        filter: invert(var(--white-invert-factor));
    }

    .chat-settings-button svg {
        height: 20px;
        width: 20px;
        margin-left: calc(50% - 10px);
        margin-top: calc(50% - 10px);
        transition: 0.35s;
    }

    .chat-settings-open .chat-settings-button svg {
        transform: rotate(45deg);
    }

    #chat-settings {
        position: absolute;
        right: 25px;
        bottom: calc(var(--interact-top-margin) + var(--interact-height));
        height: 0px;
        width: 200px;
        visibility: hidden;
        opacity: 0;
        transition: 0.35s;
    }

    .chat-settings-open #chat-settings {
        visibility: visible;
        height: 300px;
        opacity: 1;
    }
</style>
