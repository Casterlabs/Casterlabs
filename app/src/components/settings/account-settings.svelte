<script>
    import AccountBox from "./account-settings/account-box.svelte";

</script>

<style>
    #accounts {
        margin-right: 55px;
    }

    #account-caffeine .platform-logo {
        top: 22px;
        left: 13px;
        width: 23px;
    }
</style>

<div class="no-select">
    <div id="accounts">
        <AccountBox platform="caffeine" platformName="Caffeine" />
        <AccountBox platform="twitch" platformName="Twitch" />
        <AccountBox platform="trovo" platformName="Trovo" />
        <AccountBox platform="glimesh" platformName="Glimesh" />
        <AccountBox platform="brime" platformName="Brime" />
    </div>


    <script type="module">
        import Auth from "./js/auth.mjs";
        import Koi from "./js/koi.mjs";

        const accountsContainer = document.querySelector("#accounts");
        const caffeineBox = accountsContainer.querySelector("#account-caffeine");
        const twitchBox = accountsContainer.querySelector("#account-twitch");
        const trovoBox = accountsContainer.querySelector("#account-trovo");
        const glimeshBox = accountsContainer.querySelector("#account-glimesh");
        const brimeBox = accountsContainer.querySelector("#account-brime");

        // Signin buttons are handled already.

        /* ---------------- */
        /* Signout Buttons  */
        /* ---------------- */

        caffeineBox
            .querySelector(".signout-button")
            .addEventListener("click", () => Auth.signOutUser("CAFFEINE"));

        twitchBox
            .querySelector(".signout-button")
            .addEventListener("click", () => Auth.signOutUser("TWITCH"));

        trovoBox
            .querySelector(".signout-button")
            .addEventListener("click", () => Auth.signOutUser("TROVO"));

        glimeshBox
            .querySelector(".signout-button")
            .addEventListener("click", () => Auth.signOutUser("GLIMESH"));

        brimeBox
            .querySelector(".signout-button")
            .addEventListener("click", () => Auth.signOutUser("BRIME"));

        /* ---------------- */
        /* State Switching  */
        /* ---------------- */

        function onAccountSignin(account) {
            const box = accountsContainer.querySelector(`#account-${account.platform.toLowerCase()}`);
            const streamerName = box.querySelector(".streamer-name");
            const openChannel = box.querySelector(".open-channel");

            openChannel.href = account.link;
            streamerName.innerHTML = account.displayname;
            box.classList.add("linked");
        }

        Koi.on("account_signin", onAccountSignin);

        Koi.on("account_signout", (data) => {
            const platform = data.platform;

            const box = accountsContainer.querySelector(`#account-${platform.toLowerCase()}`);
            const streamerName = box.querySelector(".streamer-name");
            const openChannel = box.querySelector(".open-channel");

            openChannel.href = "#";
            streamerName.innerHTML = "";
            box.classList.remove("linked");
        });

        for (const data of Object.values(Auth.getSignedInPlatforms())) {
            const userData = data.userData

            if (userData) {
                onAccountSignin(userData.streamer);
            }
        }

    </script>
</div>