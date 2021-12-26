package co.casterlabs.koi.api;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.listener.KoiLifeCycleHandler;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class KoiConnection implements Closeable {
    public static final String KOI_URL = "wss://api.casterlabs.co/v2/koi";

    private KoiLifeCycleHandler listener;
    private FastLogger logger;
    private KoiSocket socket;

    private JsonObject request;

    @SneakyThrows
    public KoiConnection(@NonNull String url, @NonNull FastLogger logger, @NonNull KoiLifeCycleHandler listener, String clientId) {
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

    public KoiConnection hookStreamStatus(String username, UserPlatform platform) throws InterruptedException {
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

    public KoiConnection login(String token) throws InterruptedException {
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

                    case "ERROR":
                        listener.onError(packet.get("error").getAsString());
                        return;

                    case "EVENT":
                        JsonObject eventJson = packet.getObject("event");
                        KoiEvent event = KoiEventType.get(eventJson);

                        if (event == null) {
                            logger.warn("Unsupported event type: %s", eventJson.getString("event_type"));
                        } else {
                            KoiEventUtil.reflectInvoke(listener, event);
                        }

                        return;
                }
            } catch (Exception e) {
                FastLogger.logStatic(LogLevel.SEVERE, raw);
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
            if ((e instanceof IOException) && e.getMessage().isEmpty() && !this.isOpen()) {
                this.onClose(0, null, true);
            } else {
                logger.exception(e);
            }
        }

    }

    public void sendChat(@NonNull String message, @NonNull KoiChatterType chatter) {
        this.socket.send(
            new JsonObject()
                .put("type", "CHAT")
                .put("message", message)
                .put("chatter", chatter.name())
                .toString()
        );
    }

    public void upvoteChat(@NonNull String messageId) {
        this.socket.send(
            new JsonObject()
                .put("type", "UPVOTE")
                .put("message_id", messageId)
                .toString()
        );
    }

    public void deleteChat(@NonNull String messageId) {
        this.socket.send(
            new JsonObject()
                .put("type", "DELETE")
                .put("message_id", messageId)
                .toString()
        );
    }

}
