<svelte:options accessors />

<script>
    import User from "./user.svelte";

    import { onMount, onDestroy } from "svelte";

    let eventHandler;

    export let koiEvent = null;
    export let isDeleted = false;
    export let upvotes = koiEvent.upvotes || 0;
    export const timestamp = Date.now();

    export let sender = null; // We set this.
    let isMultiPlatform = false;
    let messageHtml = "";

    let highlight = false;

    // This all is for handling multiPlatform mode.
    {
        function onAuthUpdate({ koiAuth }) {
            isMultiPlatform = Object.keys(koiAuth).length > 1;
        }

        onDestroy(() => {
            eventHandler?.destroy();
        });

        onMount(async () => {
            eventHandler = Bridge.createThrowawayEventHandler();

            eventHandler.on("auth:update", onAuthUpdate);
            onAuthUpdate((await Bridge.query("auth")).data);
        });
    }

    function escapeHtml(unsafe) {
        return unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    if (koiEvent.event_type == "CLEARCHAT") {
        highlight = true;
        messageHtml = `<i>Chat was cleared.</i>`;
    } else if (["CHAT", "DONATION"].includes(koiEvent.event_type) /* Normal chat messages */) {
        messageHtml = escapeHtml(koiEvent.message);
        sender = koiEvent.sender;

        for (const [name, image] of Object.entries(koiEvent.emotes)) {
            messageHtml = messageHtml.split(name).join(`<img class="inline-image" title="${name}" src="${image}" />`);
        }

        for (const pattern of koiEvent.links) {
            const link = pattern.includes("://") ? pattern : "https://" + pattern;

            messageHtml = messageHtml.split(pattern).join(`<a href="${link}" rel="external">${pattern}</a>`);
        }

        if (koiEvent.donations) {
            // Caffeine Props & Trovo Spells don't appear in chat like Twitch Cheers do.
            for (const donation of koiEvent.donations) {
                messageHtml += ` <img class="inline-image" src="${donation.image}" />`;
            }
        }
    } else if (koiEvent.event_type == "CLEARCHAT") {
    }
</script>

<span class="chat-message {isDeleted ? 'is-deleted' : ''} {highlight ? 'highlighted' : ''}">
    {#if sender}
        <User {isMultiPlatform} userData={sender} />
    {/if}

    {@html messageHtml}{#if upvotes > 0}
        <sup class="upvote-counter">
            {#if upvotes < 10}
                <span class="upvote-1">{upvotes}</span>
            {:else if upvotes < 100}
                <span class="upvote-2">{upvotes}</span>
            {:else if upvotes < 1000}
                <span class="upvote-3">{upvotes}</span>
            {:else}
                <span class="upvote-4">{upvotes}</span>
            {/if}
        </sup>
    {/if}
</span>

<style>
    :global(.inline-image) {
        height: 1.3em;
        vertical-align: text-bottom;
    }

    .chat-message {
        padding-left: 10px;
        display: block;
        width: 100%;
    }

    .highlighted {
        margin-top: 15px;
        margin-bottom: 15px;
        text-align: center;
        background-color: rgba(0, 0, 0, 0.15);
    }

    .is-deleted {
        filter: grayscale(0.45);
        transition: 0.15s;
    }

    .is-deleted:hover {
        filter: grayscale(0);
        transition: 0.15s;
    }

    .upvote-counter {
        text-shadow: 1px 1px rgba(0, 0, 0, 0.75);
    }

    /* Upvotes */
    :global(.upvote-1) {
        /* 1+ */
        color: #ff00ff;
    }

    :global(.upvote-2) {
        /* 10+ */
        color: #00ff00;
    }

    :global(.upvote-3) {
        /* 100+ */
        color: #ffff00;
    }

    :global(.upvote-4) {
        /* 1000+ */
        color: #ffffff;
    }
</style>
