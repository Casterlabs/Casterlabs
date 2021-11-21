<script>
    import { onMount } from "svelte";

    export let platform;
    export let isKoi = false;

    onMount(() => {
        Bridge.emit("auth:request-oauth-signin", { platform: platform, isKoi: isKoi });
    });

    function cancelAuth() {
        Bridge.emit("auth:cancel-signin");
        history.back();
    }
</script>

<!-- svelte-ignore a11y-missing-attribute -->
<div class="has-text-centered no-select">
    <br />
    <br />
    <br />
    <div class="spinner">
        <div class="bounce1" />
        <div class="bounce2" />
        <div class="bounce3" />
    </div>
    <br />
    <br />
    <br />
    <br />
    <a on:click={cancelAuth} style="color: var(--theme);"> Want to go back? </a>
</div>

<!-- https://tobiasahlin.com/spinkit/ -->
<style>
    .spinner {
        margin: 100px auto 0;
        width: 70px;
        text-align: center;
    }

    .spinner > div {
        width: 18px;
        height: 18px;
        background-color: #333;

        border-radius: 100%;
        display: inline-block;
        -webkit-animation: sk-bouncedelay 1.4s infinite ease-in-out both;
        animation: sk-bouncedelay 1.4s infinite ease-in-out both;
    }

    .spinner .bounce1 {
        -webkit-animation-delay: -0.32s;
        animation-delay: -0.32s;
    }

    .spinner .bounce2 {
        -webkit-animation-delay: -0.16s;
        animation-delay: -0.16s;
    }

    @-webkit-keyframes sk-bouncedelay {
        0%,
        80%,
        100% {
            -webkit-transform: scale(0);
        }

        40% {
            -webkit-transform: scale(1);
        }
    }

    @keyframes sk-bouncedelay {
        0%,
        80%,
        100% {
            -webkit-transform: scale(0);
            transform: scale(0);
        }

        40% {
            -webkit-transform: scale(1);
            transform: scale(1);
        }
    }
</style>
