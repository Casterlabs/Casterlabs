<style>
    #accessibility-zoom-reset {
        color: inherit;
    }


    #accessibility-zoom-reset:hover {
        color: #7a7a7a !important;
    }

    #accessibility-zoom-reset :global(svg) {
        height: 20px;
        width: 20px;
        transform: translateY(7.5px);
    }
</style>

<div class="no-select">
    <div>
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
        <label>
            Icon
            <br />
            <div class="select">
                <select id="appearance-icon">
                    <option value="casterlabs">Casterlabs</option>
                    <option value="pride">Pride</option>
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
            <a id="accessibility-zoom-reset" class="hidden">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-rotate-ccw">
                    <polyline points="1 4 1 10 7 10"></polyline>
                    <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
                </svg>
            </a>
        </label>
    </div>


    <script type="module">
        import { setDarkMode } from "./js/ui.mjs";

        const appearanceZoom = document.querySelector("#accessibility-zoom");
        const appearanceZoomReset = document.querySelector("#accessibility-zoom-reset");
        const appearanceTheme = document.querySelector("#appearance-theme");


        /* -------- */
        /* Zoom     */
        /* -------- */
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
        appearanceTheme.addEventListener("change", () => {
            switch (appearanceTheme.value) {
                case "dark": {
                    setDarkMode(true);
                    break;
                }

                case "light": {
                    setDarkMode(false);
                    break;
                }

                case "system": {
                    break;
                }
            }
        });
    </script>
</div>