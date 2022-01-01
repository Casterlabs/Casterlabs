<script>
    import { onMount } from "svelte";

    let logo = "casterlabs";
    let color = "white";
    let versionString = "";
    let year = "";

    onMount(async () => {
        logo = (await Bridge.query("ui")).data.icon;
        year = new Date().getFullYear();

        if ((await Bridge.query("theme")).data.isDark) {
            color = "white";
        } else {
            color = "black";
        }

        const buildInfo = (await Bridge.query("build")).data;

        if (buildInfo.isDev) {
            versionString = "Development Build";
        } else {
            versionString = `Version ${buildInfo.version}-${buildInfo.commit} (${buildInfo.buildChannel})`;
        }
    });
</script>

<div class="about no-select box">
    <div class="columns">
        <div style="width: 248px !important; flex-grow: 0;">
            <img src="/img/wordmark/{logo}/{color}.svg" alt="Casterlabs Logo" style="width: 100%;" />
        </div>
        <div class="column" style="height: 72px; margin: auto; margin-left: 20px;">
            <p>Casterlabs-Caffeinated</p>
            <p>{versionString}</p>
        </div>
    </div>

    <hr />

    <a href="https://github.com/Casterlabs/Casterlabs-Caffeinated" rel="external" class="is-block highlight-on-hover" style="line-height: 3em;">
        Github
        <span style="float: right;">
            <svg
                xmlns="http://www.w3.org/2000/svg"
                width="1em"
                height="1em"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="feather feather-external-link"
                ><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" /><polyline points="15 3 21 3 21 9" /><line x1="10" y1="14" x2="21" y2="3" /></svg
            >
        </span>
    </a>

    <hr />

    <a href="https://casterlabs.co/terms-of-service" rel="external" class="is-block highlight-on-hover" style="line-height: 3em;">
        Terms of Service
        <span style="float: right;">
            <svg
                xmlns="http://www.w3.org/2000/svg"
                width="1em"
                height="1em"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="feather feather-external-link"
                ><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" /><polyline points="15 3 21 3 21 9" /><line x1="10" y1="14" x2="21" y2="3" /></svg
            >
        </span>
    </a>

    <hr />

    <a href="https://casterlabs.co/privacy-policy" rel="external" class="is-block highlight-on-hover" style="line-height: 3em;">
        Privacy Policy
        <span style="float: right;">
            <svg
                xmlns="http://www.w3.org/2000/svg"
                width="1em"
                height="1em"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="feather feather-external-link"
                ><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" /><polyline points="15 3 21 3 21 9" /><line x1="10" y1="14" x2="21" y2="3" /></svg
            >
        </span>
    </a>

    <hr />

    <div>
        <br />
        <p>Made with ♥ by Casterlabs.</p>
        <br />
        <p>Copyright {year} Casterlabs. All rights reserved.</p>
    </div>
</div>

<style>
    .about {
        width: 550px;
        margin: auto;
        margin-top: 25px;
        padding-left: 0;
        padding-right: 0;
    }

    .about > a,
    .about > div {
        padding-left: 15px;
        padding-right: 15px;
    }

    hr {
        margin: 0;
    }

    :global(.app-is-light) hr {
        background-color: rgb(202, 202, 202);
    }
</style>
