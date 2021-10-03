<script>
    import { setPageProperties } from "../__layout.svelte";

    import { onMount } from "svelte";

    setPageProperties({
        showSideBar: true,
        pageTitle: "",
        allowNavigateBackwards: true
    });

    onMount(async () => {
        const ChatMessage = (await import("../../components/chat/chat-message.svelte")).default;
        const Draggable = (await import("../../../static/js/util/draggable.mjs")).default;
        const { appStore } = window.Caffeinated;
        const Koi = window.Koi;

        const chatBox = document.querySelector("#chat-box>ul");

        // Chat
        {
            function addChatMessage(chatEvent) {
                const container = document.createElement("li");

                chatBox.appendChild(container);

                new ChatMessage({
                    target: container,
                    props: {
                        chatEvent: chatEvent
                    }
                });
            }

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

            Koi.on("chat", addChatMessage);
            Koi.on("donation", addChatMessage);

            // TODO implement the others.

            Koi.on("meta", onMeta);

            for (const event of Koi.history) {
                switch (event.event_type) {
                    case "CHAT":
                    case "DONATION": {
                        addChatMessage(event);
                        break;
                    }

                    case "META": {
                        onMeta(event);
                        break;
                    }
                }
            }
        }

        // Add the viewers box and make it draggable
        {
            const VIEWERS_BOX_OPTIONS = {
                limit: true,

                posX: 0,
                posY: 0,

                width: 0.05,
                height: 0.05,

                minWidth: 0.15,
                minHeight: 0.15,

                maxWidth: 1,
                maxHeight: 1,

                ...(appStore.get("page.chat.viewers_box") ?? {}),

                zIndex: 15
            };

            const draggable = new Draggable(document.querySelector("#viewers-box"), VIEWERS_BOX_OPTIONS);

            draggable.enabled = true;

            function sendUpdate() {
                const posAndSize = draggable.getPositionAndSize();

                appStore.set("page.chat.viewers_box", posAndSize);
            }

            document.addEventListener("mouseup", sendUpdate);
            draggable.on("resize", sendUpdate);
            draggable.on("move", sendUpdate);
        }
    });
</script>

<div class="stream-chat-container">
    <div class="chat-content">
        <div id="viewers-box" class="draggable hidden" />
        <div id="chat-box" class="allow-select">
            <ul />
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

    .draggable {
        overflow: hidden;
        position: sticky;
    }

    #viewers-box {
        background-color: blue;
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
