
// Basically https://stackoverflow.com/a/8809472
function generateUUID() {
    let micro = (performance && performance.now && (performance.now() * 1000)) || 0;
    let millis = new Date().getTime();

    return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
        let random = Math.random() * 16;

        if (millis > 0) {
            random = (millis + random) % 16 | 0;
            millis = Math.floor(millis / 16);
        } else {
            random = (micro + random) % 16 | 0;
            micro = Math.floor(micro / 16);
        }

        return ((c === "x") ? random : ((random & 0x3) | 0x8)).toString(16);
    });
}

function generateUnsafePassword(len = 32) {
    const chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    return Array(len)
        .fill(chars)
        .map((x) => {
            return x[Math.floor(Math.random() * x.length)]
        }).join("");
}

function generateUnsafeUniquePassword(len = 32) {
    return generateUUID().replace(/-/g, "") + generateUnsafePassword(len);
}

function getRandomItemInArray(arr) {
    const rnd = Math.floor(Math.random() * arr.length);
    return arr[rnd];
}

function sleep(millis) {
    return new Promise((resolve) => setTimeout(resolve, millis));
}

function prettifyString(str) {
    let splitStr = str.toLowerCase().split("_");

    if (splitStr.length == 0) {
        return splitStr[0].charAt(0).toUpperCase() + splitStr[0].substring(1);
    } else {
        for (let i = 0; i < splitStr.length; i++) {
            splitStr[i] = splitStr[i].charAt(0).toUpperCase() + splitStr[i].substring(1);
        }

        return splitStr.join(" ");
    }
}

function putInClipboard(copy) {
    navigator.clipboard.writeText(copy);
}

function getObjProperty(obj, path) {
    return path
        .split(".")
        .reduce((prev, curr) => {
            return prev && prev[curr]
        }, obj);
}

function setObjProperty(obj, path, value) {
    const pathParts = path.split(".");
    const finalProperty = pathParts.pop();
    let currentNest = obj;

    for (const pathPart of pathParts) {
        const nextNest = currentNest[pathPart];

        if (nextNest === undefined) {
            currentNest[pathPart] = {};
            currentNest = currentNest[pathPart];
        } else {
            currentNest = nextNest;
        }
    }

    currentNest[finalProperty] = value;
}

export {
    generateUUID,
    generateUnsafePassword,
    generateUnsafeUniquePassword,
    getRandomItemInArray,
    sleep,
    prettifyString,
    putInClipboard,
    getObjProperty,
    setObjProperty
};