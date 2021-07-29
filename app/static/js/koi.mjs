import { EventHandler } from "./util/eventhandler.mjs";

const eventHandler = new EventHandler();

// eventHandler.on("*", (type, data) => {
//     console.debug("[Koi]", `(${type})`, data);
// });

let history = [];

eventHandler.on("account_signin", (data) => {
    console.debug("[Koi]", `(account_signin)`, data.platform);
});

eventHandler.on("account_signout", (data) => {
    console.debug("[Koi]", `(account_signout)`, data.platform);
});

eventHandler.on("no_account", () => {
    console.debug("[Koi]", `(no_account)`, "No account.");
});

eventHandler.on("*", (type, event) => {
    if ([
        "CHAT",
        "DONATION",
        "META",
        "CLEARCHAT",
        "CHANNEL_POINTS",
        "FOLLOW",
        "SUBSCRIPTION",
    ].includes(type.toUpperCase())) {
        Object.freeze(event);
        history.push(event);
    }
});

const Koi = {

    // Add the event handler
    addEventListener: eventHandler.on, // Deprecated
    on: eventHandler.on,
    once: eventHandler.once,
    removeListener: eventHandler.removeListener,
    broadcast: eventHandler.broadcast,

    get history() {
        return history;
    },

    get viewerList() {
        return [];
    },

    upvote(messageId) {
        this.broadcast(`x_koi_upvotechat`, {
            messageId: messageId
        });
    },

    deleteMessage(messageId) {
        this.broadcast(`x_koi_deletechat`, {
            messageId: messageId
        });
    },

    sendMessage(message, platform) {
        if (message.startsWith("/caffeinated")) {
            this.broadcast("x_caffeinated_command", { text: message });
        } else {
            if (platform !== "CAFFEINE") {
                // Newlines only work on Caffeine.
                message = message.replace(/\n/gm, " ");
            }

            // Cut to length.
            message = message.substring(0, this.getMaxLength(platform));

            this.broadcast("x_koi_sendchat", {
                message: message,
                platform: platform.toUpperCase(),
                chatter: "CLIENT" // chatter
            });
        }
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
        this.broadcast("x_koi_test", {
            eventType: eventType
        });
    }

};

Object.freeze(Koi);

export default Koi;