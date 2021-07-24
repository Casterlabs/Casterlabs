<script>
    import PageAttributes from "../../components/page-attributes.svelte";
    import HideSideBar from "../../components/displaymodes/hide-sidebar.svelte";
    import OAuthSignin from "../../components/platforms/oauth-signin.svelte"

</script>

<PageAttributes allowNavigateBackwards="true" />
<HideSideBar />

<div id="signin-container">
    <OAuthSignin platform="glimesh" />
</div>