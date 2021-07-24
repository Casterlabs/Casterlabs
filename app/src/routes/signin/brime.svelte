<script>
    import PageAttributes from "../../components/page-attributes.svelte";
    import HideSideBar from "../../components/displaymodes/hide-sidebar.svelte";

</script>

<PageAttributes allowNavigateBackwards="true" />
<HideSideBar />

<style>
    .input,
    .button {
        width: 280px;
    }
</style>

<div id="signin-container" class="has-text-centered no-select">
    <br />
    <br />
    <br />
    <h1 class="title is-4">
        Sign in with Brime
    </h1>
    <br />
    <div>
        <input id="email-input" class="input" type="text" placeholder="Email" />
        <br />
        <input id="password-input" class="input" type="password" placeholder="Password" />
        <br />
        <br />
        <span id="error-message" class="hidden is-6"></span>
        <button id="signin-submit" class="button">
            Sign In
        </button>
        <br />
        <br />
        <a onclick="history.back()" style="color: var(--theme);">
            Want to go back?
        </a>
    </div>

    <script type="module">
        import Auth from "./js/auth.mjs";
        import Router from "./js/router.mjs";

        const signinContainer = document.querySelector("#signin-container");
        const emailInput = signinContainer.querySelector("#email-input");
        const passwordInput = signinContainer.querySelector("#password-input");
        const errorMessage = signinContainer.querySelector("#error-message");
        const signinSubmit = signinContainer.querySelector("#signin-submit");

        /* ------------ */
        /* Helpers      */
        /* ------------ */

        function clearFields() {
            emailInput.value = "";
            passwordInput.value = "";
            clearError();
        }

        function clearError() {
            emailInput.classList.remove("is-danger");
            passwordInput.classList.remove("is-danger");
            errorMessage.innerHTML = "";
            errorMessage.classList.add("hidden");
        }

        function setErrorMessage(message) {
            errorMessage.innerHTML = `${message} <br /><br />`;
            errorMessage.classList.remove("hidden");
        }

        /* ------------ */
        /* UX Features  */
        /* ------------ */

        // Make it so when you hit enter on the username input 
        // it'll automagically take you to the password field.
        emailInput.addEventListener("keyup", (e) => {
            if (e.code == "Enter") {
                passwordInput.focus();
            }
        });

        // Make the password field trigger a signin attempt on enter.
        passwordInput.addEventListener("keyup", (e) => {
            if (e.code == "Enter") {
                signinSubmit.click();
            }
        });

        /* ------------ */
        /*    *Magic*   */
        /* ------------ */

        signinSubmit.addEventListener("click", async () => {
            clearError();

            try {
                const token = await Auth.signinBrime(emailInput.value, passwordInput.value);

                Auth.addUserAuth("BRIME", token)
                    .then(() => {
                        clearFields();
                        Router.tryHomeGoBack();
                    })
                    .catch(() => {
                        setErrorMessage("An internal Casterlabs error occured, please report this to us.");
                    });
            } catch (e) {

                switch (e[0]) {
                    case "UNAUTHORIZED: Invalid Email or Password. ": {
                        emailInput.classList.add("is-danger");
                        passwordInput.classList.add("is-danger");
                        passwordInput.focus();
                        setErrorMessage("The username or password is incorrect.");
                        break;
                    }

                    case "UNAUTHORIZED: You are banned.": {
                        emailInput.classList.add("is-danger");
                        passwordInput.classList.add("is-danger");
                        setErrorMessage("You are banned from Brime.");
                        break;
                    }

                }

            }
        });

    </script>
</div>