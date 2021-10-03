<script>
    import PretzelRocksButton from "./music-services/pretzelrocks-button.svelte";
    import SpotifyButton from "./music-services/spotify-button.svelte";
</script>

<div class="no-select">
    <div id="accounts">
        :)
        <PretzelRocksButton />
        <SpotifyButton />
    </div>

    <script type="module">
    </script>
</div>

<style>
    #accounts {
        margin-right: 55px;
    }
</style>
