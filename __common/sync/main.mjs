import Chokidar from "chokidar";
import fs from "fs";

const DIRECTORIES = [
    "../lib",
    "../../caffeinated/ui/src/lib" // Caffeinated
];

async function syncUnlink(from, filePath) {
    for (const directory of DIRECTORIES) {
        if (directory == from) continue;
        fs.unlink(`${directory}/${filePath}`, () => {});
    }
}

async function syncChange(from, filePath) {
    const contents = fs.readFileSync(`${from}/${filePath}`);

    for (const directory of DIRECTORIES) {
        if (directory == from) continue;
        fs.writeFile(`${directory}/${filePath}`, contents, () => {});
    }
}

async function syncDirAdd(from, filePath) {
    for (const directory of DIRECTORIES) {
        if (directory == from) continue;
        fs.mkdir(`${directory}/${filePath}`, () => {});
    }
}

async function syncDirUnlink(from, filePath) {
    for (const directory of DIRECTORIES) {
        if (directory == from) continue;
        fs.rmdir(`${directory}/${filePath}`, { recursive: true }, () => {});
    }
}

for (const directory of DIRECTORIES) {
    const watcher = Chokidar.watch(directory, { persistent: true, ignoreInitial: true });

    watcher
        .on("add", (path) => {
            path = path.substring(directory.length);
            // console.debug("File added:", directory, path);
            syncChange(directory, path);
        })
        .on("unlink", (path) => {
            path = path.substring(directory.length);
            // console.debug("File deleted:", directory, path);
            syncUnlink(directory, path);
        })
        .on("change", (path) => {
            path = path.substring(directory.length);
            // console.debug("File changed:", directory, path);
            syncChange(directory, path);
        })
        .on("addDir", (path) => {
            path = path.substring(directory.length);
            // console.debug("Directory added:", directory, path);
            syncDirAdd(directory, path);
        })
        .on("unlinkDir", (path) => {
            path = path.substring(directory.length);
            // console.debug("Directory deleted:", directory, path);
            syncDirUnlink(directory, path);
        })
        .on("error", (error) => {
            console.error(error);
        });
}