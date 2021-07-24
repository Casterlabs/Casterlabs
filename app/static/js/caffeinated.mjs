const Store = require("electron-store");
const path = require("path");

const PROTOCOLVERSION = 95;
const VERSION = "1.2-beta5";
const SUPPORTED_PLATFORMS = ["CAFFEINE", "TWITCH", "TROVO", "GLIMESH", "BRIME"];

// Globalize PROTOCOLVERSION, VERSION, and SUPPORTED_PLATFORMS
{
    Object.defineProperty(window, "PROTOCOLVERSION", {
        value: PROTOCOLVERSION,
        writable: false,
        configurable: false
    });

    Object.defineProperty(window, "VERSION", {
        value: VERSION,
        writable: false,
        configurable: false
    });

    Object.defineProperty(window, "SUPPORTED_PLATFORMS", {
        value: SUPPORTED_PLATFORMS,
        writable: false,
        configurable: false
    });
}

// Cute debug statements
{
    console.log("%c0", `
        line-height: 105px;
        background-image: url("https://assets.casterlabs.co/logo/casterlabs_full_white.png");
        background-size: contain;
        background-repeat: no-repeat;
        background-position: center;
        background-color: #141414;
        border-radius: 15px;
        margin-left: calc((50% - 150px) - 1ch);
        padding-left: 150px;
        color: transparent;
        padding-right: 150px;
    `);
    console.log(`%c\nCaution, here be dragons!\nIf someone tells you to paste code here, they might be trying to steal important data from you.\n`, "font - size: 18px;");
    console.log("\n\n");
}

// Misc UI stuff.
window.addEventListener("load", () => {
    document.querySelector(".app-logo").title = VERSION;
});

// Initialize Stores
const storePath = path.join(app.getPath("userData"), "config");

const store_AuthDefaults = {};
// Populate.
for (const supportedPlatform of SUPPORTED_PLATFORMS) {
    store_AuthDefaults[supportedPlatform] = null;
}

const appStore = new Store({
    defaults: {
        appearance: {
            theme: "dark",
            logo: "casterlabs"
        },
        status_integration: {
            discord: {
                enabled: true,
                icon: "casterlabs-logo"
            }
        }
    },
    name: "app",
    cwd: storePath
});

const authStore = new Store({
    defaults: store_AuthDefaults,
    name: "auth",
    cwd: storePath
});

const moduleStore = new Store({
    name: "module",
    cwd: storePath
});

export {
    // Stores
    appStore,
    authStore,
    moduleStore,

};
