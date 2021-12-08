<script>
    import User from "./user.svelte";

    export let chatEvent = null;
    export let isMultiPlatform = false;

    function escapeHtml(unsafe) {
        return unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    let messageHtml = escapeHtml(chatEvent.message);
    let upvotesHtml = chatEvent.upvotes == 0 ? "" : chatEvent.upvotes;

    for (const [name, image] of Object.entries(chatEvent.emotes)) {
        messageHtml = messageHtml.split(name).join(`<img class="inline-image" title="${name}" src="${image}" />`);
    }

    for (const pattern of chatEvent.links) {
        const link = pattern.includes("://") ? pattern : "https://" + pattern;

        messageHtml = messageHtml.split(pattern).join(`<a href="${link}" rel="external">${pattern}</a>`);
    }

    if (chatEvent.donations) {
        // Caffeine Props & Trovo Spells don't appear in chat like Twitch Cheers do.
        for (const donation of chatEvent.donations) {
            messageHtml += ` <img class="inline-image" src="${donation.image}" />`;
        }
    }
</script>

<li>
    <span data-id={chatEvent.id} data-sender={chatEvent.sender.UPID}>
        <User {isMultiPlatform} userData={chatEvent.sender} />
        {messageHtml}<sup class="upvote-counter upvote-1">
            {upvotesHtml}
        </sup>
    </span>
</li>

<style>
    :global(.inline-image) {
        height: 1.3em;
        vertical-align: text-bottom;
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
