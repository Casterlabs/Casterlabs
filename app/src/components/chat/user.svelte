<script>
    export let userData;

    function prettifyString(str) {
        return str.substring(0, 1).toUpperCase() +
            str.substring(1).toLowerCase();
    }
</script>

<style>
    .inline-image {
        height: 1.3em;
        vertical-align: text-bottom;
    }

    .profile-picture {
        border-radius: 50%;
    }

    .badge:not(:first-child) {
        margin-left: .5ch;
    }
</style>

<span style="color: {userData.color}">
    <a href="{userData.link}" rel="external">
        <img src="{userData.image_link}" class="inline-image profile-picture" alt="{userData.displayname}" />
    </a><span title="{prettifyString(userData.platform)}">
        {userData.displayname}
    </span><span>
        {#each userData.badges as badge}
        <img src="{badge}" class="inline-image badge" alt="Badge" />
        {/each}
    </span>
</span>