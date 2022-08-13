import EventHandler from "./event-handler.mjs";

const eventHandler = new EventHandler();

let store = {
    language: "en-US",
    emojiProvider: "system",
    theme: "co.casterlabs.dark"
};

let appStyleElement;

function recomputeStyle() {
    const { emojiProvider } = store;

    if (emojiProvider == "system") {
        appStyleElement.innerHTML = ``;
    } else {
        appStyleElement.innerHTML = `
        [data-rich-type="emoji"] > [data-emoji-provider="system"] {
            display: none;
        }
        
        [data-rich-type="emoji"] > [data-emoji-provider="${emojiProvider}"] {
            display: inline-block !important;
        }
        `;
    }
}

function init() {
    appStyleElement = document.createElement("style");
    appStyleElement.id = "app-style";
    document.head.appendChild(appStyleElement);
    recomputeStyle();
}

export default {
    store() {
        return store;
    },

    on(key, listener) {
        const listenerId = eventHandler.on(key, listener);

        if (store[key]) {
            listener(store[key]);
        }

        return [key, listenerId];
    },

    off: eventHandler.off,

    get(key) {
        return store[key];
    },

    mutate(key, value) {
        if (!appStyleElement) {
            init();
        }

        if (store[key] !== value) {
            store[key] = value;
            eventHandler.emit(key, value);

            if (["emojiProvider"].includes(key)) {
                recomputeStyle();
            }
        }
    }
};