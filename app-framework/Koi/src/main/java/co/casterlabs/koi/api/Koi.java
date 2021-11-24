package co.casterlabs.koi.api;

import java.io.Closeable;
import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.listener.EventUtil;
import co.casterlabs.koi.api.types.events.Event;
import co.casterlabs.koi.api.types.events.EventType;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Koi implements Closeable {
    public static final String KOI_URL = "wss://api.casterlabs.co/v2/koi";

    private KoiEventListener listener;
    private FastLogger logger;
    private KoiSocket socket;

    private JsonObject request;

    @SneakyThrows
    public Koi(@NonNull String url, @NonNull FastLogger logger, @NonNull KoiEventListener listener, String clientId) {
        this.logger = logger;
        this.listener = listener;
        this.socket = new KoiSocket(new URI(url + "?client_id=" + clientId));
    }

    @Override
    public void close() {
        this.socket.close();
    }

    public boolean isConnected() {
        return this.socket.isOpen();
    }

    public Koi hookStreamStatus(String username, UserPlatform platform) throws InterruptedException {
        if (this.isConnected()) {
            throw new IllegalStateException("Already connected.");
        } else {
            this.request = new JsonObject();

            this.request.put("type", "USER_STREAM_STATUS");
            this.request.put("username", username);
            this.request.put("platform", platform.name());
            this.request.put("nonce", "_login");

            this.socket.connectBlocking();

            return this;
        }
    }

    public Koi login(String token) throws InterruptedException {
        if (this.isConnected()) {
            throw new IllegalStateException("Already connected.");
        } else {
            this.request = new JsonObject();

            this.request.put("type", "LOGIN");
            this.request.put("token", token);
            this.request.put("nonce", "_login");

            this.socket.connectBlocking();

            return this;
        }
    }

    private class KoiSocket extends WebSocketClient {

        public KoiSocket(URI uri) {
            super(uri);

            this.addHeader("User-Agent", "Casterlabs");
            this.setTcpNoDelay(true);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            if (request == null) {
                this.close();
            } else {
                this.send(request.toString());
                request = null;
            }
        }

        @Override
        public void send(String text) {
            logger.debug("\u2191 " + text);

            super.send(text);
        }

        private void keepAlive(JsonElement nonce) {
            JsonObject request = new JsonObject();

            request.put("type", "KEEP_ALIVE");
            request.put("nonce", nonce);

            this.send(request.toString());
        }

        @Override
        public void onMessage(String raw) {
            logger.debug("\u2193 " + raw);

            try {
                JsonObject packet = Rson.DEFAULT.fromJson(raw, JsonObject.class);

                switch (packet.get("type").getAsString()) {
                    case "KEEP_ALIVE":
                        this.keepAlive(packet.get("nonce"));
                        return;

                    case "SERVER":
                        listener.onServerMessage(packet.get("server").getAsString());
                        return;

                    case "EVENT":
                        JsonObject eventJson = packet.getObject("event");
                        Event event = EventType.get(eventJson);

                        if (event == null) {
                            logger.warn("Unsupported event type: %s", eventJson.getString("event_type"));
                        } else {
                            EventUtil.reflectInvoke(listener, event);
                        }

                        return;
                }
            } catch (Exception e) {
                listener.onException(e);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            if (listener == null) {
                if (remote) {
                    logger.info("Lost connection to Koi.");
                }
            } else {
                // So the user can immediately reconnect without
                // errors from the underlying library.
                new Thread(() -> listener.onClose(remote)).start();
            }
        }

        @Override
        public void onError(Exception e) {
            listener.onException(e);
        }

    }

}
