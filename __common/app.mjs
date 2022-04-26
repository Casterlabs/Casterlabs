import EventHandler from './event-handler.mjs';

const eventHandler = new EventHandler();

let store = {};

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
        if (store[key] !== value) {
            store[key] = value;
            eventHandler.emit(key, value);
        }
    }

};