<div class="no-select">
    <div>
        <!-- svelte-ignore a11y-label-has-associated-control -->
        <label>
            Theme
            <br />
            <div class="select">
                <select id="appearance-theme">
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
                <select id="appearance-icon">
                    <option value="casterlabs">Casterlabs</option>
                    <option value="pride">Pride</option>
                    <option value="moonlabs">Moonlabs</option>
                </select>
            </div>
        </label>
    </div>
    <br />
    <div>
        <label>
            Zoom
            <br />
            <input id="accessibility-zoom" type="range" min="-2" max="2" step=".01" list="accessibility-zoom-list" />
            <a id="accessibility-zoom-reset" class="hidden fade-on-hover">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-rotate-ccw">
                    <polyline points="1 4 1 10 7 10" />
                    <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" />
                </svg>
            </a>
        </label>
    </div>

    <script type="module">
        import { getTheme, setTheme, getLogo, setLogo } from "./js/ui.mjs";

        /* -------- */
        /* Zoom     */
        /* -------- */
        const appearanceZoom = document.querySelector("#accessibility-zoom");
        const appearanceZoomReset = document.querySelector("#accessibility-zoom-reset");

        let zoomValue = webFrame.getZoomLevel();

        function checkZoomReset() {
            if (zoomValue == 0) {
                appearanceZoomReset.classList.add("hidden");
            } else {
                appearanceZoomReset.classList.remove("hidden");
            }
        }

        appearanceZoomReset.addEventListener("click", () => {
            appearanceZoom.value = 0;
            webFrame.setZoomLevel(0);
            zoomValue = 0;
            appearanceZoomReset.classList.add("hidden");
        });

        appearanceZoom.addEventListener("change", () => {
            zoomValue = parseFloat(appearanceZoom.value);
            webFrame.setZoomLevel(zoomValue);
            checkZoomReset();
        });

        window.addEventListener("zoom_changed", () => {
            zoomValue = webFrame.getZoomLevel();
            appearanceZoom.value = zoomValue;
            checkZoomReset();
        });

        appearanceZoom.value = zoomValue;
        setTimeout(checkZoomReset, 500); // There seems to be a race on this, so this is the "fix".

        /* -------- */
        /* Theme    */
        /* -------- */
        const appearanceTheme = document.querySelector("#appearance-theme");

        appearanceTheme.value = getTheme();

        appearanceTheme.addEventListener("change", () => {
            setTheme(appearanceTheme.value);
        });

        /* -------- */
        /* Logo     */
        /* -------- */
        const appearanceIcon = document.querySelector("#appearance-icon");

        appearanceIcon.value = getLogo();

        appearanceIcon.addEventListener("change", () => {
            setLogo(appearanceIcon.value);
        });
    </script>
</div>

<style>
    #accessibility-zoom-reset {
        color: inherit;
    }

    #accessibility-zoom-reset :global(svg) {
        height: 20px;
        width: 20px;
        transform: translateY(7.5px);
    }
</style>
