<script>
    import { setPageProperties } from "../__layout.svelte";
    import OAuthSignin from "../../components/platforms/oauth-signin.svelte";

    setPageProperties({
        showSideBar: false,
        pageTitle: "",
        allowNavigateBackwards: true
    });
</script>

<div id="signin-container">
    <OAuthSignin platform="caffeinated_spotify" isKoi={false} />
</div>
