<script>
    import { setPageProperties } from "../__layout.svelte";

    import ChatMessage from "../../components/chat/chat-message.svelte";

    import { onMount, onDestroy } from "svelte";

    const DISPLAYABLE_EVENTS = ["FOLLOW", "CHAT", "DONATION", "SUBSCRIPTION", "VIEWER_JOIN", "VIEWER_LEAVE", "RAID", "CHANNEL_POINTS", "CLEARCHAT"];

    let eventHandler;

    let chatHistory = {};
    let isMultiPlatform = true;
    let chatbox;
    let signedInPlatforms = [];

    let showChatSettings = false;

    let chatSendPlatform = "TWITCH";
    let chatSendPlatformOpen = false;
    let chatSendMessage = "";

    let showCommandPalette = false;
    let selectedCommandIndex = 0;
    let commandPalette = [];
    let maxCommandIndex = 0;

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
                    koiEvent: event
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

    function generateCommandPalette() {
        let commandSections = [];

        // commandSections.push({
        // TODO Caffeinated Commands
        // });

        if (signedInPlatforms.includes("TWITCH")) {
            commandSections.push({
                title: "Twitch",
                platform: "TWITCH",
                commands: [
                    {
                        command: "/ban",
                        args: ["[username]", "[reason]"],
                        description: "Permanently bans a user from chatting"
                    },
                    {
                        command: "/unban",
                        args: ["[username]"],
                        description: "Removes a timeout or permenant ban on a user"
                    },

                    {
                        command: "/timeout",
                        args: ["[username]", "[duration]", "[reason]"],
                        description: "Temporarily bans a user from chatting"
                    },
                    {
                        command: "/untimeout",
                        args: ["[username]"],
                        description: "Removes a timeout ban on a user"
                    },

                    {
                        command: "/clear",
                        description: "Clears the chat history"
                    },

                    {
                        command: "/emoteonly",
                        description: "Only allows emotes in chat"
                    },
                    {
                        command: "/emoteonlyoff",
                        description: "Disable emote-only mode"
                    },

                    {
                        command: "/followers",
                        args: ["[duration]"],
                        description: "Restricts chat to followers based on the duration they've been following"
                    },
                    {
                        command: "/followersoff",
                        description: "Disables followers-only mode"
                    },

                    {
                        command: "/subscribers",
                        description: "Restricts chat to subscribers only"
                    },
                    {
                        command: "/subscribersoff",
                        description: "Disables subscribers-only mode"
                    },

                    {
                        command: "/host",
                        args: ["[channel]"],
                        description: "Host another stream on your channel"
                    },
                    {
                        command: "/unhost",
                        description: "Stops hosting the current hosted stream"
                    },

                    {
                        command: "/marker",
                        args: ["[description]"],
                        description: "Adds a stream market at the current timestamp"
                    },

                    {
                        command: "/mod",
                        args: ["[username]"],
                        description: "Grants moderator status to a user"
                    },
                    {
                        command: "/unmod",
                        args: ["[username]"],
                        description: "Revokes a user's moderator status"
                    },

                    {
                        command: "/vip",
                        args: ["[username]"],
                        description: "Grants VIP status to a user"
                    },
                    {
                        command: "/unvip",
                        args: ["[username]"],
                        description: "Revokes a user's VIP status"
                    },

                    {
                        command: "/raid",
                        args: ["[channel]"],
                        description: "Sends your viewers to another channel when your stream ends"
                    },
                    {
                        command: "/unraid",
                        description: "Cancels the ongoing raid"
                    },

                    {
                        command: "/restrict",
                        args: ["[username]"],
                        description: "Starts restricting a user's messages"
                    },
                    {
                        command: "/restrict",
                        args: ["[username]"],
                        description: "Stops restricting a user's messages"
                    },

                    {
                        command: "/slow",
                        args: ["[duration]"],
                        description: "Limit how frequently users can send messages in chat"
                    },
                    {
                        command: "/slowoff",
                        description: "Disables slow mode"
                    },

                    {
                        command: "/uniquechat",
                        description: "Prevents users from sending duplicate messages in chat"
                    },
                    {
                        command: "/uniquechatoff",
                        description: "Disables unique-chats only mode"
                    }
                ]
            });
        }

        if (signedInPlatforms.includes("CAFFEINE")) {
            commandSections.push({
                title: "Caffeine",
                platform: "CAFFEINE",
                commands: [
                    {
                        command: "/afterparty",
                        args: ["[channel]"],
                        description: "Starts an afterparty and sends your viewers to the speficied channel"
                    }
                ]
            });
        }

        // Generate the indexes.
        let idx = 0;
        for (const section of commandSections) {
            for (const command of section.commands) {
                command.index = idx;
                idx++;
            }
        }

        maxCommandIndex = idx - 1;

        return commandSections;
    }

    function commandPaletteListener(e) {
        if (showCommandPalette) {
            // These keys are used to navigate the command palette.
            if (e.key == "ArrowUp") {
                e.preventDefault();
                selectedCommandIndex = selectedCommandIndex < 0 ? maxCommandIndex : selectedCommandIndex - 1;
                return;
            } else if (e.key == "ArrowDown") {
                e.preventDefault();
                selectedCommandIndex = (selectedCommandIndex + 1) % maxCommandIndex;
                return;
            } else if (e.key == "Escape") {
                // No selection.
                selectedCommandIndex = -1;
                e.preventDefault();
                return;
            } else if (e.key == "Enter" || e.key == "Tab") {
                // Auto-complete the command.
                if (selectedCommandIndex != -1) {
                    const hasCommandFilled = chatSendMessage.startsWith(getSelectedCurrentCommand().command);

                    if (hasCommandFilled) {
                        // If they completed a command and it's still filled
                        // we shouldn't try to complete it again and should just send.
                        sendChatMessage();
                    } else {
                        e.preventDefault();
                        completeCommandPalette(selectedCommandIndex);
                        return;
                    }
                }
            }
        }
    }

    function sendChatMessage(e) {
        // Timeout is required for the binds to be updated.
        setTimeout(() => {
            // The keyboard event is from the input itself.
            if (e instanceof KeyboardEvent) {
                // If the user presses enter then we should send the message and clear everything.
                if (e.key == "Enter") {
                    showCommandPalette = false;
                    selectedCommandIndex = -1;

                    // Fall out of the if statement.
                } else {
                    // Shows the commands popup when you start your message with '/'.
                    showCommandPalette = chatSendMessage.startsWith("/");

                    // Reset the index.
                    if (showCommandPalette) {
                        commandPalette = generateCommandPalette();
                    } else {
                        selectedCommandIndex = -1;
                    }

                    // The input was NOT a signal to send, so we return.
                    return;
                }
            }

            console.log(chatSendPlatform, ">", chatSendMessage);
            chatSendMessage = "";
        }, 0);
    }

    function changeSendPlatform(platform) {
        chatSendPlatform = platform;
        chatSendPlatformOpen = false;
    }

    function openChatSendPlatformDropdown() {
        chatSendPlatformOpen = true;
    }

    function toggleChatSettings() {
        showChatSettings = !showChatSettings;
    }

    function completeCommandPalette(command) {
        if (typeof command == "number") {
            completeCommandPalette(getSelectedCurrentCommand().command);
        } else {
            chatSendMessage = command + " ";
        }
    }

    function getSelectedCurrentCommand() {
        // Loop through all the commands and find the one with the correct index.
        for (const section of commandPalette) {
            for (const c of section.commands) {
                if (c.index == selectedCommandIndex) {
                    return c;
                }
            }
        }
    }

    function onAuthUpdate({ koiAuth }) {
        signedInPlatforms = Object.keys(koiAuth);
        isMultiPlatform = signedInPlatforms.length > 1;

        if (!signedInPlatforms.includes(chatSendPlatform)) {
            chatSendPlatform = signedInPlatforms[0];
        }
    }

    onDestroy(() => {
        eventHandler?.destroy();
    });

    onMount(async () => {
        eventHandler = Bridge.createThrowawayEventHandler();

        window.test = chatHistory;

        eventHandler.on("auth:update", onAuthUpdate);
        onAuthUpdate((await Bridge.query("auth")).data);

        eventHandler.on("koi:event", processEvent);
        (await Bridge.query("koi:history")).data.forEach(processEvent);
    });
</script>

<div class="stream-chat-container {showChatSettings ? 'chat-settings-open' : ''} {showCommandPalette && generateCommandPalette().length > 0 ? 'chat-command-palette-open' : ''}">
    <div id="chat-box" class="allow-select">
        <ul bind:this={chatbox} />
    </div>

    <div id="chat-settings" class="box">some settings here.</div>

    <div id="chat-command-palette" class="box">
        {#each commandPalette as commandSection}
            <div class="command-section">
                <h1 class="title is-size-6 is-light" style="margin-left: 8px; margin-bottom: 7px; font-weight: 700;">
                    {commandSection.platform}
                </h1>
                {#each commandSection.commands as command}
                    <div
                        class="command {selectedCommandIndex == command.index ? 'highlight' : ''}"
                        style="padding-left: 8px; padding-bottom: 4px; border-radius: 3px; cursor: pointer;"
                        on:click={() => completeCommandPalette(command.command)}
                        on:mouseenter={() => (selectedCommandIndex = command.index)}
                    >
                        <span class="command-name">
                            <span class="command-name-text is-size-6" style="font-weight: 500;">
                                {command.command}
                            </span>
                            {#if command.args}
                                {#each command.args as arg}
                                    <span class="command-name-arg">{arg}</span>&nbsp;
                                {/each}
                            {/if}
                        </span>
                        <span class="command-description">
                            <h2 class="subtitle is-size-7 is-light">{command.description}</h2>
                        </span>
                    </div>
                {/each}
            </div>
        {/each}
    </div>

    <div class="interact-box-container">
        <div id="interact-box">
            <div class="field has-addons">
                {#if isMultiPlatform}
                    <div class="control">
                        <div class="dropdown is-up {chatSendPlatformOpen ? 'is-active' : ''}">
                            <div class="dropdown-trigger">
                                <button class="button" aria-haspopup="true" aria-controls="chat-send-platform" on:click={openChatSendPlatformDropdown}>
                                    <span>
                                        <img
                                            src="/img/services/{chatSendPlatform.toLowerCase()}/icon.svg"
                                            alt={chatSendPlatform}
                                            style="height: 18px; width: 18px; filter: invert(var(--white-invert-factor)); margin-top: 8px;"
                                        />
                                    </span>
                                </button>
                            </div>
                            <div class="dropdown-menu" id="chat-send-platform" role="menu">
                                <div class="dropdown-content" style="width: 52px;">
                                    {#each signedInPlatforms as platform}
                                        <!-- svelte-ignore a11y-missing-attribute -->
                                        <a class="highlight-on-hover is-block" style="height: 30px;" on:click={() => changeSendPlatform(platform)}>
                                            <div class="dropdown-item">
                                                <img
                                                    src="/img/services/{platform.toLowerCase()}/icon.svg"
                                                    alt={platform}
                                                    style="height: 18px; width: 18px; filter: invert(var(--white-invert-factor));"
                                                />
                                            </div>
                                        </a>
                                    {/each}
                                </div>
                            </div>
                        </div>
                    </div>
                {/if}
                <div class="control is-expanded" style="position: relative;">
                    <input class="input" type="text" placeholder="Send a message" bind:value={chatSendMessage} on:keydown={commandPaletteListener} on:keypress={sendChatMessage} />

                    <!-- svelte-ignore a11y-missing-attribute -->
                    <a class="chat-settings-button heavy-highlight-on-hover" on:click={toggleChatSettings} style="display: none;">
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
                    <button class="button" on:click={sendChatMessage}> Send </button>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
    :root {
        --interact-height: 40.5px;
        --interact-margin: 15px;
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
        bottom: calc(var(--interact-margin) + var(--interact-height) + var(--interact-margin));
        left: 0;
        right: 0;
        font-size: 1.05em;
        padding-top: 10px;
        overflow-y: auto;
        overflow-x: hidden;
    }

    .stream-chat-container::before {
        content: "";
        position: absolute;
        top: -10px;
        left: 0;
        width: 100%;
        height: 10px;
        box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.05);
        z-index: 5;
    }

    .stream-chat-container::after {
        content: "";
        position: absolute;
        bottom: calc(var(--interact-margin) + var(--interact-height) + var(--interact-margin) - 10px);
        left: 0;
        width: 100%;
        height: 10px;
        box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.3);
        z-index: 5;
    }

    .interact-box-container {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: calc(var(--interact-margin) + var(--interact-height) + var(--interact-margin));
        background-color: var(--background-color);
        z-index: 10;
    }

    #interact-box {
        position: absolute;
        bottom: var(--interact-margin);
        left: var(--interact-margin);
        right: var(--interact-margin);
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
        right: 20px;
        bottom: calc(var(--interact-margin) + var(--interact-height));
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

    /* Command Palette */

    .highlight {
        background-color: rgba(0.5, 0.5, 0.5, 0.15) !important;
    }

    #chat-command-palette {
        position: absolute;
        left: 66px;
        right: 83px;
        bottom: calc(var(--interact-margin) + var(--interact-height));
        height: 0px;
        visibility: hidden;
        opacity: 0;
        overflow-y: auto;
        transition: 0.35s;
    }

    .chat-command-palette-open #chat-command-palette {
        visibility: visible;
        height: 200px;
        opacity: 1;
    }
</style>
