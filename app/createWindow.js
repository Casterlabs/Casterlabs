// Modules to control application life and create native browser window
const { app, BrowserWindow } = require("electron");
const path = require("path");
const windowStateKeeper = require("electron-window-state");

// Used in the packaged environment.
const serve = require("electron-serve");
const loadURL = serve({ directory: "__sapper__/export" });

const isDev = !app.isPackaged;

const MIN_WIDTH = 800;
const MIN_HEIGHT = 600;

function createWindow(appDir) {
    const baseDir = isDev ? path.join(appDir, "static") : path.join(appDir, "__sapper__/export");

    const mainWindowState = windowStateKeeper({
        defaultWidth: MIN_WIDTH,
        defaultHeight: MIN_HEIGHT,
        file: "main-window.json"
    });

    // Create the browser window.
    let mainWindow = new BrowserWindow({
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

    if (isDev) {
        // Load the local sapper server when in dev environment.
        mainWindow.loadURL("http://localhost:3000/");
    } else {
        // Use electron-serve to serve the sapper export.
        loadURL(mainWindow);
    }

    // Emitted when the window is closed.
    mainWindow.on("closed", function () {
        // Dereference the window object, usually you would store windows
        // in an array if your app supports multi windows, this is the time
        // when you should delete the corresponding element.
        mainWindow = null;
    });

    // Emitted when the window is ready to be shown
    // This helps in showing the window gracefully.
    mainWindow.once("ready-to-show", () => {
        mainWindow.show();
    });

    return mainWindow;
}

module.exports = createWindow;