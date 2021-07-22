<script>
    import PageAttributes from "../components/page-attributes.svelte";
    import HideSideBar from "../components/displaymodes/hide-sidebar.svelte";

    import TwitchButton from "../components/platforms/login-buttons/twitch.svelte";
    import TrovoButton from "../components/platforms/login-buttons/trovo.svelte";
    import CaffeineButton from "../components/platforms/login-buttons/caffeine.svelte";
    import GlimeshButton from "../components/platforms/login-buttons/glimesh.svelte";
    import BrimeButton from "../components/platforms/login-buttons/brime.svelte";

</script>

<PageAttributes />
<HideSideBar />

<div class="has-text-centered no-select">
    <br />
    <br />
    <br />
    <br />
    <h1 class="title is-4">
        Sign into your favorite platform.
    </h1>
    <div class="login-buttons">
        <TwitchButton />
        <TrovoButton />
        <CaffeineButton />
        <GlimeshButton />
        <BrimeButton />
    </div>
</div>