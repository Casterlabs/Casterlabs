<script>
    import { onDestroy, onMount } from "svelte";
    import { Koi } from "../../../routes/app/__layout.svelte";

    import { KinokoV1 } from "$lib/kinoko.mjs";

    let kinoko;

    let isSigningIn = false;
    let pairingCode = "";

    function onClick() {
        isSigningIn = true;
        pairingCode = (Math.random() * 100000000).toFixed(0); // Random 8 digit code

        kinoko.connect(`casterlabs_pairing:${pairingCode}`, "parent");
    }

    function cancelSignin() {
        isSigningIn = false;
        kinoko.disconnect();
    }

    onMount(() => {
        kinoko = new KinokoV1();

        kinoko.on("message", ({ message }) => {
            if (message == "what") {
                kinoko.send("what:Casterlabs Studio:PLATFORM_AUTH", false);
            } else if (message.startsWith("token:")) {
                kinoko.send("success", false);

                const token = message.substring("token:".length);

                localStorage.setItem("cl_studio:usertoken", token);
                Koi.connect(token);
            }
        });
    });

    onDestroy(() => {
        kinoko.disconnect();
    });
</script>

<div class="no-select signin-button">
    <button class="button has-text-centered {isSigningIn ? 'is-loading' : ''}" on:click={onClick}>
        <div class="platform-logo">
            <img src="/img/grid.svg" alt="Pairing Code Icon" />
        </div>
        <span> Pairing Code </span>
    </button>
</div>

{#if isSigningIn}
    <div class="modal is-active">
        <div class="modal-background" />
        <div class="modal-content">
            <div class="pairing-modal">
                <div class="box" style="max-width: 350px; margin: auto;">
                    <br />
                    Open the settings in Caffeinated (beta),
                    <br />
                    Goto <b>Pair A Device</b>,
                    <br />
                    And enter the following code:
                    <br />
                    <br />
                    <div class="field">
                        <div class="control">
                            <input class="input centered-field" type="text" placeholder="Pairing Code" value={pairingCode} readonly />
                        </div>
                    </div>
                    <br />
                </div>
            </div>
        </div>
        <button class="modal-close is-large" aria-label="close" on:click={cancelSignin} />
    </div>
{/if}

<style>
    .button {
        width: 200px;
        margin-top: 1px;
        overflow: hidden;
        color: #dbdbdb;
        transition: 0.5s;
        background-color: #2c2c2c;
    }

    .button:hover {
        transition: 0.5s;
        background-color: #2f2f2f;
    }

    .button span {
        z-index: 2;
    }

    .platform-logo {
        position: absolute;
        top: 8px;
        left: 11px;
        width: 23px;
    }

    .pairing-modal {
        position: fixed;
        top: 135px;
        left: 0;
        right: 0;
    }

    .centered-field {
        text-align: center;
    }

    .centered-field::-webkit-input-placeholder {
        text-align: center;
    }

    .centered-field:-moz-placeholder {
        text-align: center;
    }
</style>
