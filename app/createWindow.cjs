// Modules to control application life and create native browser window
const { app, BrowserWindow, globalShortcut } = require("electron");
const path = require("path");
const windowStateKeeper = require("electron-window-state");

// Used in the packaged environment.
const serve = require("electron-serve");
const loadURL = serve({ directory: "build" });

const isDev = !app.isPackaged;

const MIN_WIDTH = 800;
const MIN_HEIGHT = 600;

let mainWindow;

function reloadPage() {
    if (isDev) {
        // Load the local sapper server when in dev environment.
        mainWindow.loadURL("http://localhost:3000/");
    } else {
        // Use electron-serve to serve the sapper export.
        loadURL(mainWindow);
    }
}

function createWindow(appDir) {
    const baseDir = isDev ? path.join(appDir, "static") : path.join(appDir, "build");

    const mainWindowState = windowStateKeeper({
        defaultWidth: MIN_WIDTH,
        defaultHeight: MIN_HEIGHT,
        file: "main-window.json"
    });

    // Create the browser window.
    mainWindow = new BrowserWindow({
        minWidth: MIN_WIDTH,
        minHeight: MIN_HEIGHT,
        width: mainWindowState.width,
        height: mainWindowState.height,
        x: mainWindowState.x,
        y: mainWindowState.y,
        transparent: false,
        resizable: true,
        show: false,
        icon: path.join(baseDir, "logo-512.png"),
        frame: false,
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false,
            enableRemoteModule: true,
            webSecurity: false
        }
    });

    reloadPage();

    // Emitted when the window is closed.
    mainWindow.on("closed", () => {
        // Dereference the window object, usually you would store windows
        // in an array if your app supports multi windows, this is the time
        // when you should delete the corresponding element.
        mainWindow = null;
    });

    // Emitted when the window is ready to be shown
    // This helps in showing the window gracefully.
    mainWindow.once("ui-theme-loaded", () => {
        mainWindow.show();
    });

    return mainWindow;
}


// Intercept CTRL+R or F5.
{
    const reloadKeybinds = [
        "CommandOrControl+Shift+R",
        "CommandOrControl+R",
        "F5"
    ];

    const autoUnregister = [
        ...reloadKeybinds,
        "Alt+F5"
    ];

    app.on("browser-window-focus", () => {
        if (!isDev) {
            for (const intercept of reloadKeybinds) {
                globalShortcut.register(intercept, () => {
                    console.debug("[Framework]", intercept, "was blocked.");
                    // reloadPage();
                });
            }
        }

        globalShortcut.register("Alt+F5", () => {
            console.debug("[Framework]", "Alt+F5 was pressed, relaunching client.");
            app.relaunch();
            app.exit(0);
        });
    });

    app.on("browser-window-blur", () => {
        for (const intercept of autoUnregister) {
            globalShortcut.unregister(intercept);
        }
    });
}

module.exports = createWindow;