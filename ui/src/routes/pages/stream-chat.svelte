<script>
    import { setPageProperties } from "../__layout.svelte";

    import ChatMessage from "../../components/chat/chat-message.svelte";

    import { onMount, onDestroy } from "svelte";

    let eventHandler;

    let chatHistory = [];
    let isMultiPlatform = true;

    setPageProperties({
        showSideBar: true,
        pageTitle: "",
        allowNavigateBackwards: true
    });

    function onMeta(event) {
        const chatElement = chatBox.querySelector(`[data-id='${event.id}']`);

        if (event.is_visible) {
            const counterElement = chatElement.querySelector(".upvote-counter");

            if (event.upvotes > 0) {
                counterElement.innerText = event.upvotes;

                if (event.upvotes >= 1000) {
                    counterElement.classList = "upvote-4";
                } else if (event.upvotes >= 100) {
                    counterElement.classList = "upvote-3";
                } else if (event.upvotes >= 10) {
                    counterElement.classList = "upvote-2";
                } else if (event.upvotes >= 1) {
                    counterElement.classList = "upvote-1";
                }
            }
        } else {
            chatElement.remove();
        }
    }

    // TODO better processing of messages, this works for now.
    function processEvent(event) {
        console.log(event);

        switch (event.event_type) {
            case "CHAT":
            case "DONATION": {
                chatHistory.push(event);
                break;
            }

            case "META": {
                onMeta(event);
                break;
            }
        }

        chatHistory = chatHistory; // Svelte update.
    }

    onDestroy(() => {
        eventHandler?.destroy();
    });

    onMount(async () => {
        eventHandler = Bridge.createThrowawayEventHandler();

        eventHandler.on("koi:event", processEvent);
        (await Bridge.query("koi")).data.history.forEach(processEvent);
    });
</script>

<div class="stream-chat-container">
    <div class="chat-content">
        <div id="chat-box" class="allow-select">
            <ul>
                {#each chatHistory as chatEvent}
                    <ChatMessage {chatEvent} bind:isMultiPlatform />
                {/each}
            </ul>
        </div>
    </div>
    <div id="interact-box" />
</div>

<style>
    :root {
        --interact-height: 30px;
    }

    .stream-chat-container {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
    }

    .chat-content {
        height: 100%;
    }

    #chat-box {
        position: absolute;
        top: 0;
        bottom: var(--interact-height);
        left: 0;
        right: 0;
        font-size: 1.05em;
        margin-top: 5px;
        margin-left: 10px;
    }

    #interact-box {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: var(--interact-height);
    }
</style>
