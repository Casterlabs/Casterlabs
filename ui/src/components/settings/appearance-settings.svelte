<script>
    import { onDestroy, onMount } from "svelte";

    let eventHandler;

    // let zoomValue;
    let appearanceTheme;
    let appearanceIcon;

    function sendUpdatedPreferences() {
        Bridge.emit("ui:appearance-update", {
            theme: appearanceTheme,
            icon: appearanceIcon
        });
    }

    function loadPreferences(data) {
        appearanceTheme = data.theme;
        appearanceIcon = data.icon;
    }

    onDestroy(() => {
        eventHandler.destroy();
    });

    onMount(async () => {
        eventHandler = Bridge.createThrowawayEventHandler();
        eventHandler.on("pref-update:ui", loadPreferences);
        loadPreferences((await Bridge.query("ui")).data);
    });
</script>

<div class="no-select">
    <div>
        <!-- svelte-ignore a11y-label-has-associated-control -->
        <label>
            Theme
            <br />
            <div class="select">
                <select id="appearance-theme" bind:value={appearanceTheme} on:change={sendUpdatedPreferences}>
                    <option value="dark">Dark</option>
                    <option value="light">Light</option>
                    <!-- <option value="system">Match System Preference</option> -->
                </select>
            </div>
        </label>
    </div>
    <br />
    <div>
        <!-- svelte-ignore a11y-label-has-associated-control -->
        <label>
            Icon
            <br />
            <div class="select">
                <select id="appearance-icon" bind:value={appearanceIcon} on:change={sendUpdatedPreferences}>
                    <option value="casterlabs">Casterlabs</option>
                    <option value="pride">Pride</option>
                    <option value="moonlabs">Moonlabs</option>
                </select>
            </div>
        </label>
    </div>
</div>
