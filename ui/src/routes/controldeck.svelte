<script>
    import DummyDeck from "../components/controldeck/ui/impl/dummydeck.svelte";

    import { setPageProperties } from "./__layout.svelte";

    setPageProperties({
        showSideBar: true,
        pageTitle: "Control Deck",
        allowNavigateBackwards: true
    });
</script>

<br />
<div class="has-text-centered">
    <DummyDeck deckId="DUMMY000-0000-0000-0000-DUMMYDECK000" />
</div>
