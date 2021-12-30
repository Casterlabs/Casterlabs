package co.casterlabs.caffeinated.pluginsdk.music;

import java.util.HashMap;
import java.util.Map;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonObject;

public class Music {

    private static Map<String, MusicProvider> providers = new HashMap<>();
    private static MusicProvider activePlayback;

    /**
     * @deprecated This is used internally.
     */
    @Deprecated
    public static JsonObject toJson() {
        JsonElement _activePlayback;

        if (activePlayback == null) {
            _activePlayback = JsonNull.INSTANCE;
        } else {
            _activePlayback = activePlayback.toJson();
        }

        return new JsonObject()
            .put("activePlayback", _activePlayback)
            .put("providers", Rson.DEFAULT.toJson(providers));
    }

}
