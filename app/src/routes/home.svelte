<script>
    import { setPageProperties } from "./__layout.svelte";

    setPageProperties({
        showSideBar: true,
        pageTitle: "",
        allowNavigateBackwards: false
    });
</script>

<div class="has-text-centered">
    <br />
    <br />
    <h1 class="title">test page :^)</h1>
    <br />

    <a href="/welcome/step1">welcome/step1</a>
    <br />
    <a href="/settings">settings</a>
    <br />
    <a href="/pages/stream-chat">pages/stream-chat</a>
    <br />
    <a href="/signin">login</a>
    <br />
    <a href="/">index</a>

    <br />
    <br />
    <a href="https://casterlabs.co" rel="external">casterlabs</a>
</div>
