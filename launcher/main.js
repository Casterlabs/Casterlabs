// Modules to control application life and create native browser window
const { app, BrowserWindow } = require("electron");
const path = require("path");
const Store = require("electron-store");
const electronDl = require("electron-dl");
const fetch = require("node-fetch");
const fs = require("fs");
const DecompressZip = require("decompress-zip");

// Used in the packaged environment.
const serve = require("electron-serve");
const loadURL = serve({ directory: "__sapper__/export" });

// Setup some globals
const LAUNCHER_VERSION = 4;

global.PACKAGED = app.isPackaged;
const isDev = !PACKAGED;
const baseDir = isDev ? path.join(process.cwd(), "static") : path.join(__dirname, "__sapper__/export");

const directory = path.join(app.getPath("userData"), "update");
const file = "app.asar";

// Init the store
const store = new Store();

if (!store.get("channel")) {
    store.set("channel", "STABLE");
}

if (!store.get("protocol_version")) {
    store.set("protocol_version", -1);
}

store.set("launcher_version", LAUNCHER_VERSION);


// Keep a global reference of the window object. If you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let mainWindow;

// Updater
function setStatus(line1 = "&nbsp;", line2 = "&nbsp;") {
    mainWindow.webContents.executeJavaScript(`setStatus(${JSON.stringify(line1)}, ${JSON.stringify(line2)});`);
}

function sleep(millis) {
    return new Promise((resolve) => setTimeout(resolve, millis));
}

function updateAsarExists() {
    const file = path.join(app.getPath("appData"), "update/app.asar");

    try {
        return fs.existsSync(file);
    } catch (e) {
        console.error(e);
        return false;
    }
}

async function checkForUpdates() {
    setStatus("Checking for updates");

    try {
        const currentProtocolVersion = store.get("protocol_version") ?? -1;

        const updates = await (await fetch("https://api.casterlabs.co/v1/caffeinated/updates")).json();
        const launcher = updates["launcher-" + LAUNCHER_VERSION] || updates["launcher-1"];
        let channel = launcher[store.get("channel")];

        if (!channel) {
            console.warn("Invalid channel: " + store.get("channel"));
            store.set("channel", "STABLE");
            channel = launcher["STABLE"];
        }

        if (channel.web_url) {
            mainWindow.loadURL(channel.web_url);
        } else if ((currentProtocolVersion < channel.protocol_version) || !updateAsarExists()) {
            setStatus("Downloading update");
            downloadUpdate(channel.asar_url, channel.deps_url);
        } else {
            setStatus("You're up-to-date! 😄");
            await sleep(1500);
            setStatus();
            await sleep(1000);
        }
    } catch (e) {
        let left = 15;

        console.log(e);

        while (left > 0) {
            left--;
            setStatus("Update failed", `Retrying in ${left} seconds.`);
            await sleep(1000);
        }

        setStatus();
        await sleep(1000);

        checkForUpdates();
    }
}

async function downloadUpdate(url, deps) {
    console.log(`Downloading update from ${url} to ${directory}${file}`);

    try {
        fs.unlinkSync(directory);
    } catch (ignored) { }

    await electronDl.download(mainWindow, url, {
        directory: directory,
        filename: file,
        onProgress(progress) {
            const downloadedMB = kFormatter(progress.transferredBytes, 1);
            const totalMB = kFormatter(progress.totalBytes, 1);
            const percent = Math.floor(progress.percent * 100);

            setStatus("Downloading update", `${percent}% (${downloadedMB} / ${totalMB})`);
        }
    });

    if (deps) {
        console.log(`Downloading dependencies from ${deps} to ${directory} /deps.zip`);

        await electronDl.download(mainWindow, deps, {
            directory: directory,
            filename: "deps.zip",
            onProgress(progress) {
                const downloadedMB = kFormatter(progress.transferredBytes, 1);
                const totalMB = kFormatter(progress.totalBytes, 1);
                const percent = Math.floor(progress.percent * 100);

                setStatus("Downloading dependencies", `${percent}% (${downloadedMB} / ${totalMB})`);
            }
        });

        const unzipper = new DecompressZip(path.join(directory, "deps.zip"));

        unzipper.on("error", (e) => {
            console.error(e);
        });

        unzipper.on("extract", () => {
            loadUpdatedFile();
        });

        setStatus("Extracting dependencies");

        console.log(`Unzipping dependencies from ${directory}deps.zip to ${directory}`);

        unzipper.extract({
            path: directory,
            restrict: false
        });
    } else {
        loadUpdatedFile();
    }
}

function loadUpdatedFile() {
    setStatus();

    console.info("Thanks for using CaffeinatedLauncher! *bows*");

    try {
        // Load the main.js.
        require(path.join(directory, "app.asar/createWindow.js"));

        mainWindow.close();
        mainWindow = null;
    } catch (e) {
        setStatus("An error occurred.", "Exiting in 5 seconds.");
        console.error(e);

        sleep(5000).then(() => {
            app.exit();
        });
    }
}

function createUpdaterWindow() {
    // Create the browser window.
    mainWindow = new BrowserWindow({
        width: 500,
        height: 320,
        icon: path.join(baseDir, "logo-512.png"),
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false,
            enableRemoteModule: true,
            webSecurity: false
        },
        transparent: false,
        resizable: false,
        show: false,
        frame: false
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

    mainWindow.once("check-for-updates", checkForUpdates);

    // Emitted when the window is ready to be shown
    // This helps in showing the window gracefully.
    mainWindow.once("ready-to-show", () => {
        mainWindow.show();
    });
}

// Utility
function kFormatter(num, decimalPlaces = 1, threshold = 1000) {
    let shortened;
    let mult;

    if ((num >= threshold) && (num >= 1000)) {
        if (num >= 1099511627776) {
            shortened = "Over 1";
            mult = "TB";
        } else if (num >= 1073741824) {
            shortened = (num / 1000000000).toFixed(decimalPlaces);
            mult = "GB";
        } else if (num >= 1048576) {
            shortened = (num / 1000000).toFixed(decimalPlaces);
            mult = "MB";
        } else if (num >= 1024) {
            shortened = (num / 1000).toFixed(decimalPlaces);
            mult = "KB";
        }
    } else {
        shortened = num.toFixed(decimalPlaces);
        mult = "B";
    }

    if (shortened.includes(".")) {
        shortened = shortened.replace(/\.?0+$/, '');
    }

    return shortened + mult;
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on("ready", createUpdaterWindow);

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
        createUpdaterWindow();
    }
});
