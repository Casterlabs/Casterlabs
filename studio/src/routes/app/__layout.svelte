<!-- This is mostly to give me the ability to use svelte logic in a global context. -->
<script context="module">
    import KoiConn from "../../components/koi.mjs";

    export const Koi = new KoiConn();

    let loggedIn = false;

    export function getToken() {
        return localStorage.getItem("cl_studio:usertoken");
    }

    export function connectKoi(token = getToken()) {
        if (token && !isKoiLoggedIn()) {
            localStorage.setItem("cl_studio:usertoken", token);
            Koi.connect(token);
            return true;
        } else {
            return false;
        }
    }

    export function signout() {
        localStorage.removeItem("cl_studio:usertoken");
        location.href = "/app";
    }

    export function isKoiLoggedIn() {
        return loggedIn;
    }

    Koi.on("close", () => {
        setTimeout(() => {
            Koi.connect(getToken());
        }, 5000);
    });

    Koi.on("error", (event) => {
        const error = event.error;

        switch (error) {
            // case "PUPPET_AUTH_INVALID": {
            //     break;
            // }

            case "USER_AUTH_INVALID": {
                loggedIn = false;
                localStorage.removeItem("cl_studio:usertoken");
                location.href = "/app";
                break;
            }
        }
    });

    Koi.on("user_update", (event) => {
        if (!loggedIn) {
            loggedIn = true;
            Koi.emit("account_signin", event.streamer);
        }
    });
</script>

<script>
    import { onMount } from "svelte";

    onMount(async () => {
        if (!window.__common) {
            window.__common = await import("$lib/__common.mjs");
        }

        window.Koi = Koi;
    });
</script>

<svelte:head>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;600&display=swap" />
</svelte:head>

<slot />
