<script>
    export let platform;
</script>

<!-- https://tobiasahlin.com/spinkit/ -->
<style>
    .spinner {
        margin: 100px auto 0;
        width: 70px;
        text-align: center;
    }

    .spinner>div {
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
            -webkit-transform: scale(0)
        }

        40% {
            -webkit-transform: scale(1.0)
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
            -webkit-transform: scale(1.0);
            transform: scale(1.0);
        }
    }
</style>

<div class="has-text-centered no-select">
    <span id="signin-platform" class="hidden">{platform}</span>
    <br />
    <br />
    <br />
    <div class="spinner">
        <div class="bounce1"></div>
        <div class="bounce2"></div>
        <div class="bounce3"></div>
    </div>
    <br />
    <br />
    <br />
    <br />
    <a id="signin-go-back" style="color: var(--theme);">
        Want to go back?
    </a>

    <script type="module">
        import Auth from "./js/auth.mjs";
        import Router from "./js/router.mjs";

        const platform = document.querySelector("#signin-platform").innerText.toUpperCase();

        Auth.signinOAuth(platform)
            .then(async (token) => {
                try {
                    if (platform == "SPOTIFY") {
                        await MusicIntegration.SPOTIFY.loginOauth(token);
                        history.back();
                    } else {
                        await Auth.addUserAuth(platform, token);
                        Router.tryHomeGoBack();
                    }
                } catch (e) {
                    history.back();
                }
            })
            .catch(() => {
                history.back();
            });

        document
            .querySelector("#signin-go-back")
            .addEventListener("click", () => {
                Auth.cancelOAuthSignin();
                history.back();
            });
    </script>
</div>