package co.casterlabs.caffeinated.app.networking.localserver;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpStatus;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.sora.api.http.SoraHttpSession;

public interface RouteHelper {

    /* -------------------- */
    /* Util                 */
    /* -------------------- */

    default boolean authorize(SoraHttpSession session) {
        String auth = session.getHeader("authorization");

        if ((auth == null) || !auth.startsWith("Key ")) {
            return false;
        }

        auth = auth.substring("Key ".length());

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

}
