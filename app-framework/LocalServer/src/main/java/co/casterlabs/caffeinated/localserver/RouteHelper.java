package co.casterlabs.caffeinated.localserver;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpStatus;
import co.casterlabs.rakurai.io.http.websocket.Websocket;
import co.casterlabs.rakurai.io.http.websocket.WebsocketCloseCode;
import co.casterlabs.rakurai.io.http.websocket.WebsocketListener;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.websockets.SoraWebsocketSession;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public interface RouteHelper {

    /* -------------------- */
    /* Util                 */
    /* -------------------- */

    default boolean authorize(SoraHttpSession session) {
        String auth = session.getQueryParameters().get("authorization");

        if (auth == null) {
            return false;
        }

        String conductorKey = CaffeinatedApp.getInstance().getAppPreferences().get().getConductorKey();

        return auth.equals(conductorKey);
    }

    default boolean authorize(SoraWebsocketSession session) {
        String auth = session.getQueryParameters().get("authorization");

        if (auth == null) {
            return false;
        }

        String conductorKey = CaffeinatedApp.getInstance().getAppPreferences().get().getConductorKey();

        return auth.equals(conductorKey);
    }

    default HttpResponse newResponse(HttpStatus status, JsonElement jsonElement) {
        JsonObject body = new JsonObject();

        body.put("errors", new JsonArray());
        body.put("data", jsonElement);

        HttpResponse response = HttpResponse.newFixedLengthResponse(status, body.toString());

        response.setMimeType("application/json");

        return response;
    }

    default HttpResponse newErrorResponse(HttpStatus status, RequestError error) {
        HttpResponse response = HttpResponse.newFixedLengthResponse(status, "{\"data\":null,\"errors\":[\"" + error + "\"]}");

        response.setMimeType("application/json");

        return response;
    }

    default WebsocketListener newWebsocketErrorResponse(HttpStatus status, RequestError error) {
        JsonObject errorPayload = this.makeWebsocketErrorPayload(status, error, true);

        return new WebsocketListener() {

            @Override
            public void onOpen(Websocket websocket) {
                new AsyncTask(() -> {
                    try {
                        websocket.send(errorPayload.toString());
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException ignored) {} finally {
                        safeClose(websocket);
                    }
                });
            }

        };
    }

    default JsonObject makeWebsocketErrorPayload(HttpStatus status, RequestError error, boolean isFatal) {
        return new JsonObject()
            .putNull("data")
            .put("errors", new JsonArray(error.name()))
            .put("type", "ERROR")
            .put(
                "status",
                new JsonObject()
                    .put("code", status.getStatusCode())
                    .put("description", status.getDescription())
                    .put("isFatal", isFatal)
            );
    }

    default void safeClose(@Nullable Websocket websocket) {
        if (websocket != null) {
            try {
                websocket.close(WebsocketCloseCode.NORMAL);
            } catch (IOException e) {
                FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst closing a connection:");
                FastLogger.logException(e);
            }
        }
    }

}
