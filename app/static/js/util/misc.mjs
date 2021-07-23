
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

export {
    generateUUID,
    generateUnsafePassword,
    generateUnsafeUniquePassword
};