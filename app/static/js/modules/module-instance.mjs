import { EventHandler } from "../util/eventhandler.mjs";
import GlobalKoi from "../koi.mjs";
import Auth from "../auth.mjs";
import { moduleStore } from "../caffeinated.mjs";
import MusicIntegration from "../music-integration/music.mjs";

function InstanceKoi() {
    const eventHandler = new EventHandler();

    const InstanceKoi = {

        // Add the event handler
        addEventListener: eventHandler.on, // Deprecated
        on: eventHandler.on,
        once: eventHandler.once,
        removeListener: eventHandler.removeListener,
        broadcast: eventHandler.broadcast,

        get history() {
            return GlobalKoi.history;
        },

        get viewerList() {
            return GlobalKoi.viewerList;
        },

        getCurrentMusicPlayback() {
            return MusicIntegration.getCurrentPlayback();
        },

        upvote(messageId) {
            GlobalKoi.broadcast(`x_koi_upvotechat`, {
                messageId: messageId
            });
        },

        deleteMessage(messageId) {
            GlobalKoi.broadcast(`x_koi_deletechat`, {
                messageId: messageId
            });
        },

        sendMessage(message, platform) {
            if (platform !== "CAFFEINE") {
                // Newlines only work on Caffeine.
                message = message.replace(/\n/gm, " ");
            }

            // Cut to length.
            message = message.substring(0, this.getMaxLength(platform));

            GlobalKoi.broadcast("x_koi_sendchat", {
                message: message,
                platform: platform.toUpperCase(),
                chatter: "CLIENT" // chatter
            });
        },

        getMaxLength(platform) {
            switch (platform) {
                case "CAFFEINE":
                    return 80;

                case "TWITCH":
                    return 500;

                case "TROVO":
                    return 300;

                case "GLIMESH":
                    return 255;

                case "BRIME":
                    return 300;

                default:
                    return 100; // ?
            }
        },

        test(eventType) {
            GlobalKoi.broadcast("x_koi_test", {
                eventType: eventType
            });
        },

        getSignedInPlatforms() {
            return Auth.getSignedInPlatforms();
        }

    };

    let globalListenerId = GlobalKoi.on("*", (type, event) => {
        InstanceKoi.broadcast(type, event);
    });

    function destroy() {
        GlobalKoi.removeListener(globalListenerId);
    }

    Object.freeze(InstanceKoi);

    return [InstanceKoi, destroy];
}

class ModuleInstance {
    #namespace = null;
    #id = null;
    #name = null;

    #frameElement = null;

    #instanceKoi = null;
    #instanceModule = null;

    #settings = {};
    #defaultSettings = {};

    destroyHandlers = [];

    constructor(namespace, id, name, scriptLocations, settings, defaultSettings) {
        this.#namespace = namespace;
        this.#id = id;
        this.#name = name;
        this.#settings = settings;
        this.#defaultSettings = defaultSettings;

        // Setup `Koi`
        {
            const [iKoi, iKoiDestroy] = new InstanceKoi();

            this.#instanceKoi = iKoi;
            this.destroyHandlers.push(iKoiDestroy);
        }

        // Setup `Module`
        {
            const eventHandler = new EventHandler();

            this.#instanceModule = {
                ...eventHandler,

                get namespace() {
                    return this.#namespace
                },

                get id() {
                    return this.#id
                },

                get name() {
                    return this.#name
                },

                get defaultSettings() {
                    return this.#defaultSettings;
                },

                get settings() {
                    return this.#settings;
                },

                set settings(newValue) {
                    this.#settings = newValue;
                    this.save();
                    this.#instanceModule.broadcast("settings_changed", this.#settings);
                }
            };

            Object.freeze(this.#instanceModule);
        }

        const CaffeinatedMeta = {
            protocolVersion: PROTOCOLVERSION,
            version: VERSION,
            supportedPlatforms: SUPPORTED_PLATFORMS,
            isDev: isDev,
            device: Device
        };

        // Setup iframe
        {
            this.#frameElement = document.createElement("iframe");

            this.#frameElement.addEventListener("load", () => {
                const { contentWindow, contentDocument } = this.#frameElement;

                Object.defineProperty(contentWindow, "Koi", {
                    value: this.#instanceKoi,
                    writable: false,
                    configurable: false
                });
                Object.defineProperty(contentWindow, "Module", {
                    value: this.#instanceModule,
                    writable: false,
                    configurable: false
                });
                Object.defineProperty(contentWindow, "Caffeinated", {
                    value: CaffeinatedMeta,
                    writable: false,
                    configurable: false
                });

                // Load scripts
                let scriptPromises = [];

                for (const scriptLocation of scriptLocations) {
                    scriptPromises.push(new Promise((resolve) => {
                        const moduleScriptElement = contentDocument.createElement("script");

                        // https://example.com/script.js#module
                        if (scriptLocation.endsWith("#module")) {
                            moduleScriptElement.setAttribute("type", "module");
                        }

                        moduleScriptElement.src = scriptLocation;
                        contentDocument.body.appendChild(moduleScriptElement);

                        moduleScriptElement.addEventListener("load", resolve);
                    }));
                }

                Promise
                    .all(scriptPromises)
                    .then(() => {
                        this.#instanceModule.broadcast("init", {
                            namespace: this.#namespace,
                            id: this.#id,
                            name: this.#name
                        });
                    });
            });

            this.#frameElement.src = `/sandbox.html?name=${this.getFullId()}`;

            document.querySelector("#sandbox-frames").appendChild(this.#frameElement);
        }
    }

    get settings() {
        return this.#settings;
    }

    set settings(newValue) {
        this.#settings = newValue;
        this.save();
        this.#instanceModule.broadcast("settings_update", this.#settings);
    }

    get name() {
        return this.#name;
    }

    set name(newValue) {
        this.#name = newValue;
        this.save();
    }

    save() {
        moduleStore.set(`${this.#namespace}.${this.#id}.settings`, this.#settings);
        moduleStore.set(`${this.#namespace}.${this.#id}.name`, this.#name);
        console.debug(`[ModuleInstance (${this.getFullId()})]`, "Saved", moduleStore.get(`${this.#namespace}.${this.#id}`));
    }

    getFullId() {
        return `${this.#namespace}:${this.#id}`;
    }

    destroy() {
        for (const des of this.destroyHandlers) {
            try {
                des();
            } catch (ignored) { }
        }

        this.#frameElement.remove();
    }

}

export default ModuleInstance;
