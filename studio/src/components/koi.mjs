import EventHandler from "$lib/event-handler.mjs";

const CLIENT_ID = "LmHG2ux992BxqQ7w9RJrfhkW";

class KoiConn {
    constructor() {
        this.eventHandler = new EventHandler();

        // Add the event handler
        this.on = this.eventHandler.on;
        this.once = this.eventHandler.once;
        this.off = this.eventHandler.off;
        this.emit = this.eventHandler.emit;
        this.createThrowawayEventHandler = this.eventHandler.createThrowawayEventHandler;

        // Add static properties
        let viewerList = null;
        let userData = null;
        let streamData = null;
        let history = [];

        // Reset properties on disconnect
        this.eventHandler.on("close", () => {
            viewerList = null;
            userData = null;
            streamData = null;
        });

        // Get properties from Koi as they come in
        this.eventHandler.on("*", (type, event) => {
            if (["CHAT", "DONATION", "META", "CLEARCHAT", "CHANNEL_POINTS", "FOLLOW", "SUBSCRIPTION"].includes(type.toUpperCase())) {
                Object.freeze(event);
                history.push(event);
            }

            switch (type.toUpperCase()) {
                case "VIEWER_LIST": {
                    viewerList = event;
                    break;
                }

                case "USER_UPDATE": {
                    userData = event;
                    break;
                }

                case "STREAM_STATUS": {
                    streamData = event;
                    break;
                }
            }
        });

        Object.defineProperty(this, "viewerList", {
            get: () => viewerList,
            configurable: false
        });
        Object.defineProperty(this, "userData", {
            get: () => userData,
            configurable: false
        });
        Object.defineProperty(this, "streamData", {
            get: () => streamData,
            configurable: false
        });
        Object.defineProperty(this, "history", {
            get: () => history,
            configurable: false
        });
    }

    connect(token) {
        if (this.ws && this.ws.readyState != WebSocket.CLOSED) {
            this.ws.close();
        } else {
            try {
                this.ws = new WebSocket(`wss://api.casterlabs.co/v2/koi?client_id=${CLIENT_ID}`);

                // let userAuthReached = false;

                this.ws.onerror = () => {
                    setTimeout(() => this.connect(token), 5000);
                };

                this.ws.onopen = () => {
                    this.emit("open");

                    this.ws.send(
                        JSON.stringify({
                            type: "LOGIN",
                            token: token
                        })
                    );
                };

                this.ws.onclose = () => {
                    this.emit("close");
                };

                this.ws.onmessage = async (payload) => {
                    const raw = payload.data;
                    const json = JSON.parse(raw);

                    if (json.type == "KEEP_ALIVE") {
                        this.ws.send(
                            JSON.stringify({
                                type: "KEEP_ALIVE"
                            })
                        );
                    } else if (json.type == "NOTICE") {
                        // const notice = json.notice;
                        // console.debug("New notice:");
                        // console.debug(notice);
                        // CAFFEINATED.triggerBanner(notice.id, (element) => {
                        //     element.innerHTML = notice.message;
                        // }, notice.color);
                    } else if (json.type == "CREDENTIALS") {
                        this.credentialCallbacks.forEach((callback) => callback.resolve(json));
                        this.credentialCallbacks = [];
                    } else if (json.type == "ERROR") {
                        if (json.error === "AUTH_INVALID") {
                            this.credentialCallbacks.forEach((callback) => callback.reject());
                            this.credentialCallbacks = [];
                        }

                        this.emit("error", json);
                    } else if (json.type == "EVENT") {
                        const event = json.event;
                        const type = event.event_type;

                        if (type === "CATCHUP") {
                            for (const catchupEvent of event.events) {
                                catchupEvent.is_catchup = true;
                                this.emit("event", catchupEvent);
                            }
                            return;
                        } else if (type === "DONATION" && event.sender.platform === "CASTERLABS_SYSTEM") {
                            const streamerPlatform = event.streamer.platform; // TODO MOVE AWAY FROM THIS
                            event.is_test = true;

                            event.donations.forEach((donation) => {
                                // TODO keep this up-to-date with new platforms.
                                if (streamerPlatform === "CAFFEINE") {
                                    donation.amount = 9;
                                    donation.currency = "CAFFEINE_CREDITS";
                                    donation.image = "https://assets.caffeine.tv/digital-items/praise.36c2c696ce186e3d57dc4ca69482f315.png";
                                    donation.animated_image = "https://assets.caffeine.tv/digital-items/praise_preview.062e1659faa201a6c9fb0f4599bfa8ef.png";
                                    donation.type = "CAFFEINE_PROP";
                                } else if (streamerPlatform === "TWITCH") {
                                    donation.amount = 100;
                                    donation.currency = "TWITCH_BITS";
                                    donation.image = "https://d3aqoihi2n8ty8.cloudfront.net/actions/party/light/static/100/4.gif";
                                    donation.animated_image = "https://d3aqoihi2n8ty8.cloudfront.net/actions/party/light/animated/100/4.gif";
                                    donation.type = "TWITCH_BITS";
                                }
                            });

                            // Add an emote to the message since that's how they work on Twitch
                            if (streamerPlatform === "TWITCH") {
                                event.message = event.message + " Party100";

                                event.emotes["Party100"] = "https://d3aqoihi2n8ty8.cloudfront.net/actions/party/light/animated/100/4.gif";
                            }
                        } else if (type === "FOLLOW" && event.follower.platform === "CASTERLABS_SYSTEM") {
                            event.is_test = true;
                        } else if (type === "CHAT" && event.sender.platform === "CASTERLABS_SYSTEM") {
                            event.is_test = true;
                        } else if (type === "SUBSCRIPTION" && event.subscriber && event.subscriber.platform === "CASTERLABS_SYSTEM") {
                            event.is_test = true;
                        }

                        // if ((type === "USER_UPDATE") && !userAuthReached) {
                        //     // Make this only execute once.
                        //     userAuthReached = true;

                        //     if (CAFFEINATED.puppetToken) {
                        //         this.ws.send(JSON.stringify({
                        //             type: "PUPPET_LOGIN",
                        //             token: CAFFEINATED.puppetToken
                        //         }));
                        //     }
                        // }

                        this.emit(event.event_type, event);
                    } else {
                        this.emit("message", json);
                    }
                };
            } catch (e) {
                console.error(e);
                throw e;
            }
        }
    }

    getCredentials() {
        return new Promise((resolve, reject) => {
            this.credentialCallbacks.push({
                resolve: resolve,
                reject: reject
            });

            this.ws.send(
                JSON.stringify({
                    type: "CREDENTIALS"
                })
            );
        });
    }

    upvoteMessage(messageId) {
        if (this.isAlive()) {
            this.ws.send(
                JSON.stringify({
                    type: "UPVOTE",
                    message_id: messageId
                })
            );
        }
    }

    deleteMessage(messageId) {
        if (this.isAlive()) {
            this.ws.send(
                JSON.stringify({
                    type: "DELETE",
                    message_id: messageId
                })
            );
        }
    }

    sendMessage(message, chatter = "CLIENT", replyTarget = null, isUserGesture = true) {
        if (this.isAlive()) {
            this.ws.send(
                JSON.stringify({
                    type: "CHAT",
                    message: message,
                    chatter: chatter,
                    replyTarget: replyTarget,
                    isUserGesture: isUserGesture
                })
            );
        }
    }

    isAlive() {
        return this.ws && this.ws.readyState == WebSocket.OPEN;
    }

    test(event) {
        if (this.isAlive()) {
            this.ws.send(
                JSON.stringify({
                    type: "TEST",
                    eventType: event.toUpperCase()
                })
            );
        }
    }

    close() {
        if (this.isAlive()) {
            this.ws.close();
        }
    }
}

export default KoiConn;
