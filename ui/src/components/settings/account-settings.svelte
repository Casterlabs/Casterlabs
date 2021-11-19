<script>
    import AccountBox from "./account-settings/account-box.svelte";

    import { onMount } from "svelte";

    let accounts = {
        caffeine: {
            accountName: "",
            accountLink: "#",
            isSignedIn: false
        },
        twitch: {
            accountName: "",
            accountLink: "#",
            isSignedIn: false
        },
        trovo: {
            accountName: "",
            accountLink: "#",
            isSignedIn: false
        },
        glimesh: {
            accountName: "",
            accountLink: "#",
            isSignedIn: false
        },
        brime: {
            accountName: "",
            accountLink: "#",
            isSignedIn: false
        }
    };

    /* ---------------- */
    /* State Switching  */
    /* ---------------- */

    function onAccountSignIn(account) {
        accounts[account.platform.toLowerCase()] = {
            accountName: account.displayname,
            accountLink: account.link,
            isSignedIn: true
        };
    }

    function onAccountSignOut(data) {
        accounts[data.platform.toLowerCase()] = {
            accountName: "",
            accountLink: "#",
            isSignedIn: false
        };
    }

    onMount(() => {
        Koi.on("account_signin", onAccountSignIn);
        Koi.on("account_signout", onAccountSignOut);

        for (const data of Object.values(Auth.getSignedInPlatforms())) {
            const userData = data.userData;

            if (userData) {
                onAccountSignIn(userData.streamer);
            }
        }

        Auth.cancelOAuthSignin();
    });

    function signout(event) {
        const platform = event.detail.platform.toUpperCase();
        window.Auth.signOutUser(platform);
    }
</script>

<div class="no-select">
    <div id="accounts">
        <!-- i know, i know, it looks messy but it works so well. -->
        <AccountBox platform="caffeine" platformName="Caffeine" signInLink="/signin/caffeine" bind:accountName={accounts.caffeine.accountName} bind:accountLink={accounts.caffeine.accountLink} bind:isSignedIn={accounts.caffeine.isSignedIn} on:signout={signout} />
        <AccountBox platform="twitch" platformName="Twitch" signInLink="/signin/twitch" bind:accountName={accounts.twitch.accountName} bind:accountLink={accounts.twitch.accountLink} bind:isSignedIn={accounts.twitch.isSignedIn} on:signout={signout} />
        <AccountBox platform="trovo" platformName="Trovo" signInLink="/signin/trovo" bind:accountName={accounts.trovo.accountName} bind:accountLink={accounts.trovo.accountLink} bind:isSignedIn={accounts.trovo.isSignedIn} on:signout={signout} />
        <AccountBox platform="glimesh" platformName="Glimesh" signInLink="/signin/glimesh" bind:accountName={accounts.glimesh.accountName} bind:accountLink={accounts.glimesh.accountLink} bind:isSignedIn={accounts.glimesh.isSignedIn} on:signout={signout} />
        <AccountBox platform="brime" platformName="Brime" signInLink="/signin/brime" bind:accountName={accounts.brime.accountName} bind:accountLink={accounts.brime.accountLink} bind:isSignedIn={accounts.brime.isSignedIn} on:signout={signout} />
    </div>
</div>

<style>
    #accounts {
        margin-right: 55px;
    }
</style>
