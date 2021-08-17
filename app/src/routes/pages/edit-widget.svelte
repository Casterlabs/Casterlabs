<script>
    import PageAttributes from "../../components/page-attributes.svelte";
    import HideSideBar from "../../components/displaymodes/hide-sidebar.svelte";

    import { onMount } from "svelte";

    let widgetSettings = [];

    onMount(async () => {

    });
</script>

<style>

</style>

<PageAttributes allowNavigateBackwards="true" title="Widget Manager" />
<HideSideBar />

<div class="has-text-left">
    {#each widgetSettings as widgetCategory}

    <!-- 
    {
        "name": "chat_direction",
        "label": "caffeinated.chat.chat_direction",
        "items": [
            {
                "name": "chat_direction",
                "type": "select",
                "defaultValue": "down",
                "options": {
                    "down": "caffeinated.chat.chat_direction.down",
                    "up": "caffeinated.chat.chat_direction.up"
                }
            }
        ]
    }
    -->

    <div>
        {#each widgetCategory.items as widgetSettingsOption}

        <!--
            {
                "name": "chat_direction",
                "type": "select",
                "defaultValue": "down",
                "options": {
                    "down": "caffeinated.chat.chat_direction.down",
                    "up": "caffeinated.chat.chat_direction.up"
                }
            }
        -->

        {/each}
    </div>

    <script>
        feather.replace();
    </script>
    {/each}

</div>