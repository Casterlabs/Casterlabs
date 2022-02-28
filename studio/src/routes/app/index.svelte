<script>
    import { onMount } from "svelte";
    import { goto } from "$app/navigation";

    import { connectKoi, Koi } from "./__layout.svelte";

    import LoadingSpinner from "$lib/components/LoadingSpinner.svelte";

    import TwitchButton from "../../components/platforms/signin-buttons/Twitch.svelte";
    import TrovoButton from "../../components/platforms/signin-buttons/Trovo.svelte";
    import GlimeshButton from "../../components/platforms/signin-buttons/Glimesh.svelte";
    import BrimeButton from "../../components/platforms/signin-buttons/Brime.svelte";
    import PairingCodeButton from "../../components/platforms/signin-buttons/PairingCode.svelte";

    let loginState = "loading";

    onMount(() => {
        if (connectKoi()) {
            loginState = "probe";
        } else {
            loginState = "login";
        }
    });

    Koi.on("account_signin", () => {
        goto("/app/chat-viewer"); // TODO multiple pages.
    });
</script>

<section class="loading has-text-centered">
    <div class="casterlabs-wordmark">
        <img class="light-show" src="/img/wordmark/casterlabs/black.svg" width="300px" height="auto" alt="Casterlabs Studio Logo" />
    </div>

    <br />
    <br />

    {#if loginState == "loading" || loginState == "probe"}
        <div class="loading-spinner">
            <LoadingSpinner />
        </div>
    {:else if loginState == "login"}
        <div class="login-container">
            <TwitchButton />
            <TrovoButton />
            <GlimeshButton />
            <BrimeButton />
            <br />
            <PairingCodeButton />
            <br />
            <br />
            <br />
            <div class="service-disclaimer" style="max-width: 80%; margin: auto;">
                By signing in, you agree to our
                <a href="https://casterlabs.co/terms-of-service" target="_blank">Terms of Service</a>
                and acknowledge that our
                <a href="https://casterlabs.co/privacy-policy" target="_blank">Privacy Policy</a>
                applies to you.
            </div>
        </div>
    {/if}
</section>

<style>
    .loading {
        position: absolute;
        top: 45px;
        left: 0;
        width: 100%;
        height: 100vh;
    }

    .casterlabs-wordmark img {
        width: 240px;
    }

    .loading-spinner {
        margin: auto;
        margin-top: 25px;
        width: 50px;
        height: 50px;
    }

    .login-container {
        margin: auto;
        margin-top: 25px;
        width: 100%;
        height: 50%;
    }

    .service-disclaimer {
        width: 320px;
    }
</style>
