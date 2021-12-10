package co.casterlabs.caffeinated.pluginsdk;

import java.util.List;
import java.util.Map;

import co.casterlabs.caffeinated.util.Reflective;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;

public class Koi {
    private static @Reflective @Getter List<KoiEvent> eventHistory;
    private static @Reflective @Getter Map<String, List<User>> viewers;
    private static @Reflective @Getter Map<String, UserUpdateEvent> userStates;
    private static @Reflective @Getter Map<String, StreamStatusEvent> streamStates;

    /**
     * @deprecated This is used internally.
     */
    @Deprecated
    public static JsonObject toJson() {
        return new JsonObject()
            .put("history", Rson.DEFAULT.toJson(eventHistory))
            .put("viewers", Rson.DEFAULT.toJson(viewers))
            .put("userStates", Rson.DEFAULT.toJson(userStates))
            .put("streamStates", Rson.DEFAULT.toJson(streamStates));
    }

}
