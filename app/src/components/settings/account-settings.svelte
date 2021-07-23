<script>
    import AccountBox from "./account-settings/account-box.svelte";

</script>

<style>
    .accounts {
        margin-right: 55px;
    }

    #account-caffeine .platform-logo {
        top: 22px;
        left: 13px;
        width: 23px;
    }
</style>

<div class="no-select">
    <div class="accounts">
        <AccountBox platform="caffeine" platformName="Caffeine" />
        <AccountBox platform="twitch" platformName="Twitch" />
        <AccountBox platform="trovo" platformName="Trovo" />
        <AccountBox platform="glimesh" platformName="Glimesh" />
        <AccountBox platform="brime" platformName="Brime" />
    </div>


    <script type="module">

    </script>
</div>