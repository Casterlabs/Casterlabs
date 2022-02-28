<script>
    import { onMount } from "svelte";

    onMount(async () => {
        if (!window.__common) {
            window.__common = await import("$lib/__common.mjs");
        }
    });
</script>

<slot />
