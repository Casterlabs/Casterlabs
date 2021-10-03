<script>
    import { setPageProperties } from "../__layout.svelte";

    setPageProperties({
        showSideBar: false,
        pageTitle: "",
        allowNavigateBackwards: true
    });
</script>

<div id="signin-container" class="has-text-centered no-select">
    <br />
    <br />
    <br />
    <h1 class="title is-4">Sign in with Caffeine</h1>
    <br />
    <div>
        <input id="username-input" class="input" type="text" placeholder="Username" />
        <br />
        <input id="password-input" class="input" type="password" placeholder="Password" />
        <br />
        <div class="hidden">
            <br />
            <input id="mfa-input" class="input is-success" type="text" placeholder="Two Factor Code" />
            <br />
        </div>
        <br />
        <span id="error-message" class="hidden is-6" />
        <button id="signin-submit" class="button"> Sign In </button>
        <br />
        <br />
        <a onclick="history.back()" style="color: var(--theme);"> Want to go back? </a>
    </div>

    <script type="module">
        import Auth from "./js/auth.mjs";
        import Router from "./js/router.mjs";

        const signinContainer = document.querySelector("#signin-container");
        const usernameInput = signinContainer.querySelector("#username-input");
        const passwordInput = signinContainer.querySelector("#password-input");
        const mfaInput = signinContainer.querySelector("#mfa-input");
        const errorMessage = signinContainer.querySelector("#error-message");
        const signinSubmit = signinContainer.querySelector("#signin-submit");

        /* ------------ */
        /* Helpers      */
        /* ------------ */

        function clearFields() {
            usernameInput.value = "";
            passwordInput.value = "";
            mfaInput.value = "";
            mfaInput.parentElement.classList.add("hidden");
            clearError();
        }

        function clearError() {
            usernameInput.classList.remove("is-danger");
            passwordInput.classList.remove("is-danger");
            mfaInput.classList.remove("is-danger");
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
        usernameInput.addEventListener("keyup", (e) => {
            if (e.code == "Enter") {
                passwordInput.focus();
            }
        });

        // Make the password and mfa fields trigger a signin attempt on enter.
        {
            passwordInput.addEventListener("keyup", (e) => {
                if (e.code == "Enter") {
                    signinSubmit.click();
                }
            });

            mfaInput.addEventListener("keyup", (e) => {
                if (e.code == "Enter") {
                    signinSubmit.click();
                }
            });
        }

        /* ------------ */
        /*    *Magic*   */
        /* ------------ */

        signinSubmit.addEventListener("click", async () => {
            clearError();

            try {
                const token = await Auth.signinCaffeine(usernameInput.value, passwordInput.value, mfaInput.value);

                Auth.addUserAuth("CAFFEINE", token)
                    .then(() => {
                        clearFields();
                        Router.tryHomeGoBack();
                    })
                    .catch(() => {
                        setErrorMessage("An internal Casterlabs error occured, please report this to us.");
                    });
            } catch (e) {
                if (e == "CAFFEINE_MFA") {
                    clearError();
                    mfaInput.parentElement.classList.remove("hidden");
                    mfaInput.focus();
                } else {
                    if (e.otp) {
                        mfaInput.classList.add("is-danger");
                        mfaInput.focus();
                        setErrorMessage("The Two Factor code is expired or incorrect.");
                    } else if (e._error) {
                        switch (e._error[0]) {
                            case "The username or password provided is incorrect": {
                                usernameInput.classList.add("is-danger");
                                passwordInput.classList.add("is-danger");
                                passwordInput.focus();
                                setErrorMessage("The username or password is incorrect.");
                                break;
                            }
                        }
                    } else {
                        setErrorMessage(JSON.stringify(e));
                        console.error(e);
                    }
                }
            }
        });
    </script>
</div>

<style>
    .input,
    .button {
        width: 280px;
    }
</style>
