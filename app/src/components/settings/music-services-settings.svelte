<script>
    import PretzelRocksButton from "./music-services/pretzelrocks-button.svelte";
    import SpotifyButton from "./music-services/spotify-button.svelte";

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
        :)
        <PretzelRocksButton />
        <SpotifyButton />
    </div>


    <script type="module">

    </script>
</div>