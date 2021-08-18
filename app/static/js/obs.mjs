const OBSWebSocket = require("obs-websocket-js");

import { appStore } from "./caffeinated.mjs";
import { EventHandler } from "./util/eventhandler.mjs";

const eventHandler = new EventHandler();
const websocket = new OBSWebSocket();

let connected = false;
let disconnectIsIntentional = false;
let audioSourceCache = [];

/* ------------ */
/* Connection   */
/* ------------ */

async function connect() {
    if (connected) {
        disconnectIsIntentional = true;
        websocket.disconnect();
    }

    const { address, password } = appStore.get("obs_integration");

    try {
        await websocket.connect({
            address: address,
            password: password
        });

        connected = true;
        eventHandler.broadcast("connected", {});

        const obsVersion = await websocket.send("GetVersion");
        console.log("[OBS]", `Connected to OBS Studio v${obsVersion.obsStudioVersion}`);
    } catch (ignored) { }
}

websocket.on("ConnectionClosed", () => {
    if (disconnectIsIntentional) {
        disconnectIsIntentional = false;
    } else {
        connected = false;
        audioSourceCache = {};
        eventHandler.broadcast("disconnected", {});

        console.warn("[OBS]", "Could not connect, retrying in 5s");
        setTimeout(connect, 5000);
    }
});

connect();

/* ------------- */
/* Audio Sources */
/* ------------- */

async function getAudioSources() {
    const response = await websocket.send("GetSourcesList");

    let audioSources = [];

    for (const source of response.sources) {
        if ([
            "wasapi_output_capture",
            "wasapi_input_capture"
        ].includes(source.typeId)) {
            audioSources.push(source);
        }
    }

    return audioSources;
}

async function getAllAudioSourcesVolume() {
    const audioSources = await getAudioSources();

    let volumePromises = [];
    let volumes = [];

    for (const source of audioSources) {
        volumePromises.push(
            websocket.send("GetVolume", {
                source: source.name
            }).then((data) => {
                const { name, volume, muted } = data;

                volumes.push(new AudioSource(name, volume, muted));
            })
        );
    }

    await Promise.all(volumePromises);

    return volumes;
}

eventHandler.on("connected", async () => {
    const audioSources = await getAllAudioSourcesVolume();

    let newCache = {};

    for (const audioSource of audioSources) {
        newCache[audioSource.name] = audioSource;
    }

    eventHandler.broadcast("audio_reset", newCache);

    audioSourceCache = newCache;
});

class AudioSource {
    #name = null;
    #volume = null;
    #muted = null;

    constructor(name, volume, muted) {
        this.#name = name;
        this.#volume = volume;
        this.#muted = muted;
    }

    get name() {
        return this.#name;
    }

    get volume() {
        return this.#volume;
    }

    set volume(newValue) {
        this.#volume = newValue;

        websocket.send("SetVolume", {
            source: this.#name,
            volume: newValue
        });
    }

    get muted() {
        return this.#muted;
    }

    set muted(newValue) {
        this.#muted = newValue;
        websocket.send("SetMute", {
            source: this.#name,
            mute: newValue
        });
    }

    // Setters that won't trigger a Set operation.
    _set(prop, val) {
        if (prop == "#name") {
            this.#name = val;
        } else if (prop == "#volume") {
            this.#volume = val;
        } else if (prop == "#muted") {
            this.#muted = val;
        }
    }

}

/* ------------ */
/* OBS Events   */
/* ------------ */

websocket.on("SourceVolumeChanged", (data) => {
    if (audioSourceCache[data.sourceName]) {
        const audioSource = audioSourceCache[data.sourceName];

        if (audioSource.volume != data.volume) {
            audioSource._set("#volume", data.volume);

            eventHandler.broadcast("audio_source_update", audioSource, false);
        }
    }
});

websocket.on("SourceMuteStateChanged", (data) => {
    if (audioSourceCache[data.sourceName]) {
        const audioSource = audioSourceCache[data.sourceName];

        if (audioSource.muted != data.muted) {
            audioSource._set("#muted", data.muted);

            eventHandler.broadcast("audio_source_update", audioSource, false);
        }
    }
});

websocket.on("SourceCreated", async (data) => {
    if ([
        "wasapi_output_capture",
        "wasapi_input_capture"
    ].includes(data.sourceKind)) {
        const { name, volume, muted } = await websocket.send("GetVolume", {
            source: data.sourceName
        });

        const audioSource = new AudioSource(name, volume, muted);

        audioSourceCache[name] = audioSource;

        eventHandler.broadcast("audio_device_created", audioSource, false);
    }
});

websocket.on("SourceDestroyed", (data) => {
    if (audioSourceCache[data.sourceName]) {
        delete audioSourceCache[data.sourceName];

        eventHandler.broadcast("audio_device_removed", {
            name: data.sourceName
        });
    }
});

websocket.on("SourceRenamed", (data) => {
    if (audioSourceCache[data.previousName]) {
        const audioSource = audioSourceCache[data.previousName];

        audioSource._set("#name", data.newName);

        audioSourceCache[data.newName] = audioSource;
        delete audioSourceCache[data.previousName];

        eventHandler.broadcast("audio_device_renamed", {
            oldName: data.previousName,
            newName: data.newName,
            audioSource: audioSource
        });
    }
});


// Debug
eventHandler.on("*", (type, data) => {
    console.debug("[OBS]", `${type}:`, data);
});

export default {
    websocket,
    eventHandler,

    reconnect: connect,

    getListOfAudioSources() {
        return getAudioSources();
    },

    getAudioSources() {
        return audioSourceCache;
    },

    isConnected() {
        return connected;
    },

};