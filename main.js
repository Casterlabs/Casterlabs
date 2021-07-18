// Modules to control application life and create native browser window
const { app, BrowserWindow } = require("electron");
const path = require("path");

// Used in the packaged environment.
const serve = require("electron-serve");
const loadURL = serve({ directory: "__sapper__/export" });

// Keep a global reference of the window object. If you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let mainWindow;

const isDev = !app.isPackaged;

const baseDir = isDev ? path.join(process.cwd(), "static") : path.join(__dirname, "__sapper__/export");

function createWindow() {
    // Create the browser window.
    mainWindow = new BrowserWindow({
        width: 1024,
        height: 768,
        webPreferences: {
            nodeIntegration: true
        },
        icon: path.join(baseDir, "logo-512.png"),
        show: false
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
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on("ready", createWindow);

// Quit when all windows are closed.
app.on("window-all-closed", () => {
    // On macOS it's common for applications and their menu bar
    // to stay active until the user quits explicitly with Cmd + Q
    if (process.platform !== "darwin") {
        app.quit();
    }
});

app.on("activate", () => {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (mainWindow === null) {
        createWindow();
    }
});
