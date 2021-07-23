import EventHandler from "./eventhandler.mjs";

function KinokoV1() {
    const eventHandler = new EventHandler();

    let ws;

    const kinoko = {
        on: eventHandler.on,
        once: eventHandler.once,
        removeListener: eventHandler.removeListener,

        isOpen() {
            return (ws && (ws.readyState == WebSocket.OPEN));
        },

        send(message, isJson = true) {
            if (this.isOpen()) {
                if (this.proxy) {
                    ws.send(message);
                } else {
                    if (isJson) {
                        ws.send(JSON.stringify(message));
                    } else {
                        ws.send(message);
                    }
                }
            }
        },

        connect(channel, type = "client", proxy = false) {
            setTimeout(() => {
                const uri = `wss://api.casterlabs.co/v1/kinoko?channel=${encodeURIComponent(channel)}&type=${encodeURIComponent(type)}&proxy=${encodeURIComponent(proxy)}`;

                this.disconnect();

                ws = new WebSocket(uri);
                this.proxy = proxy;

                ws.onerror = () => {
                    this.connect(channel, type, proxy);
                }

                ws.onopen = () => {
                    eventHandler.broadcast("open");
                };

                ws.onclose = () => {
                    eventHandler.broadcast("close");
                };

                ws.onmessage = (message) => {
                    const data = message.data;

                    switch (data) {
                        case ":ping": {
                            if (!this.proxy) {
                                ws.send(":ping");
                                return;
                            }
                        }

                        case ":orphaned": {
                            eventHandler.broadcast("orphaned");
                            return;
                        }

                        case ":adopted": {
                            eventHandler.broadcast("adopted");
                            return;
                        }

                        default: {
                            if (this.proxy) {
                                eventHandler.broadcast("message", data);
                            } else {
                                try {
                                    eventHandler.broadcast("message", JSON.parse(data));
                                } catch (ignored) {
                                    eventHandler.broadcast("message", data);
                                }
                            }
                            return
                        }
                    }
                };
            }, 1500);
        },

        disconnect() {
            if (this.isOpen()) {
                ws.close();
            }
        }

    };

    Object.freeze(kinoko);

    return kinoko;
}

export {
    KinokoV1
};