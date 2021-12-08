<script>
    export let userData;
    export let isMultiPlatform = false;

    function prettifyString(str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
</script>

<span style="color: {userData.color}">
    <a href={userData.link} rel="external">
        <img src={userData.image_link} class="inline-image profile-picture" alt={userData.displayname} />
    </a><span title={prettifyString(userData.platform)}>
        {userData.displayname}

        {#if isMultiPlatform}
            ({prettifyString(userData.platform)})
        {/if}
    </span><span>
        {#each userData.badges as badge}
            <img src={badge} class="inline-image badge" alt="Badge" />
        {/each}
    </span>
</span>

<style>
    .inline-image {
        height: 1.3em;
        vertical-align: text-bottom;
    }

    .profile-picture {
        border-radius: 50%;
    }

    .badge:not(:first-child) {
        margin-left: 0.5ch;
    }
</style>
