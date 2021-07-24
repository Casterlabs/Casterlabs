import { EventHandler } from "./eventhandler.mjs";

function KinokoV1() {
    const eventHandler = new EventHandler();

    let ws;
    let proxyMode = false;

    const kinoko = {
        on: eventHandler.on,
        once: eventHandler.once,
        removeListener: eventHandler.removeListener,

        isProxyMode() {
            return proxyMode;
        },

        isOpen() {
            return (ws && (ws.readyState == WebSocket.OPEN));
        },

        send(message, isJson = true) {
            if (this.isOpen()) {
                if (proxyMode) {
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
                proxyMode = proxy;

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
                            if (!proxyMode) {
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
                            if (proxyMode) {
                                eventHandler.broadcast("message", { message: data });
                            } else {
                                try {
                                    eventHandler.broadcast("message", { message: JSON.parse(data) });
                                } catch (ignored) {
                                    eventHandler.broadcast("message", { message: data });
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