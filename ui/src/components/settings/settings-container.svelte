<script>
    export let categories = [];
</script>

<div class="settings-container">
    <div class="settings-navigate side-bar has-text-left">
        {#each categories as category}
            {#if category.type == "section"}
                <h1 class="settings-section title is-6">
                    {category.name}
                </h1>
            {/if}

            {#if category.type == "category"}
                <!-- svelte-ignore a11y-missing-attribute -->
                <a class="settings-category-button is-6" data-for={category.id}>
                    <span>
                        {category.name}
                    </span>
                </a>
            {/if}
        {/each}
        <script type="module">
            const buttons = document.querySelectorAll(".settings-navigate .settings-category-button");

            let currentButton = null;
            let currentSection = null;

            function clearSelected() {
                currentButton?.classList.remove("is-selected");
                currentSection?.classList.add("hidden");
            }

            for (const button of buttons) {
                button.addEventListener("click", () => {
                    clearSelected();

                    button.classList.add("is-selected");
                    currentButton = button;

                    const dataFor = button.getAttribute("data-for");

                    currentSection = document.querySelector(`.settings-content [data-id='${dataFor}']`);
                    currentSection?.classList.remove("hidden");
                });
            }

            // Show the first option
            setTimeout(() => buttons[0].click(), 50); // Next js tick.
        </script>
    </div>

    <div class="settings-content has-text-left">
        <slot />
    </div>
</div>

<style>
    .settings-container {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        overflow: hidden;
    }

    .settings-navigate {
        position: absolute;
        top: 0;
        left: 0;
        width: 175px;
        height: 100%;
        padding-left: 10px;
        padding-right: 10px;
        overflow-y: auto;
    }

    .settings-content {
        position: absolute;
        top: 0;
        left: 175px;
        right: 0;
        height: 100%;
        margin-left: 20px;
        overflow-y: auto;
    }

    .settings-section {
        margin-left: 10px;
        margin-bottom: 0 !important;
    }

    .settings-section:not(:first-child) {
        margin-top: 15px;
    }

    .settings-category-button {
        color: unset !important;
        width: 100%;
        margin-right: 10px;
        border-radius: 4px;
        display: block;
    }

    .settings-category-button span {
        margin-left: 10px;
    }

    .settings-category-button:hover {
        background-color: rgba(100, 100, 100, 0.05);
    }

    .settings-category-button:global(.is-selected) {
        background-color: rgba(100, 100, 100, 0.2);
    }
</style>
