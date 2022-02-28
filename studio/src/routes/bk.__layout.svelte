<script>
    let dropdownIsOpen = false;
</script>

<div class="body-container" on:click={() => (dropdownIsOpen = false)}>
    <div id="body-content" class="container is-max-desktop">
        <slot />
    </div>
</div>

<div class="header-container">
    <nav id="header" class="container is-max-desktop {dropdownIsOpen ? 'dropdown-open' : ''}">
        <a id="burger-button" on:click={() => (dropdownIsOpen = !dropdownIsOpen)}>
            {#if dropdownIsOpen}
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="36"
                    height="36"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-x"
                >
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
            {:else}
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="36"
                    height="36"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-menu"
                >
                    <line x1="3" y1="12" x2="21" y2="12" />
                    <line x1="3" y1="6" x2="21" y2="6" />
                    <line x1="3" y1="18" x2="21" y2="18" />
                </svg>
            {/if}
            <br />
        </a>
        <ul class="header-content">
            <li>
                <a class="button is-rounded is-list" href="https://casterlabs.co" target="_blank">Casterlabs</a>
            </li>
        </ul>
    </nav>
</div>

<svelte:head>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;600&display=swap" />
    <link rel="stylesheet" href="/css/bulma.min.css" />
</svelte:head>

<style>
    :root {
        --text-color: #000000;
        --background-color: #fafafa;
        --header-size: 75px;
        --burger-width: 135px;
        --burger-height: 85px;
    }

    :global(body) {
        font-family: "Manrope", Arial, "Helvetica Neue", Helvetica, sans-serif;
    }

    .body-container {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        overflow-y: auto;
    }

    .header-container {
        position: fixed;
        left: 0;
        right: 0;
        top: 0;
        height: var(--header-size);
    }

    #header {
        padding-left: 175px;
        padding: 5px;
        text-align: right;
        position: fixed;
        top: 0;
        right: 0;
        width: 50px;
        height: 50px;
        transition: 0.5s;
        z-index: 900;
        overflow: hidden;
    }

    #burger-button {
        display: block;
        margin: auto;
        color: var(--text-color);
        z-index: 1010;
    }

    .header-content {
        text-align: center;
        position: absolute;
        top: 60px;
        right: 0;
        width: var(--burger-width);
        height: var(--burger-height);
        opacity: 0;
        transition: 0.25s;
    }

    .header-content a {
        color: var(--text-color);
        width: 90%;
    }

    #header.dropdown-open {
        width: var(--burger-width);
        height: calc(60px + var(--burger-height));
        border-radius: 0 0 0 10px;
        background-color: var(--background-color);
        box-shadow: 0 0 10px -3px var(--text-color);
    }

    .dropdown-open .header-content {
        opacity: 1;
        transition: 0.75s;
    }

    /* .header-content {
        background-color: rgb(0, 0, 255, 0.1);
    } */

    /* #body-content {
        background-color: rgb(255, 0, 0, 0.1);
    } */
</style>
