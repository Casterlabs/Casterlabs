package co.casterlabs.caffeinated.pluginsdk.koi;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.casterlabs.caffeinated.util.Reflective;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;

public class Koi {
    private static @Reflective @Getter List<KoiEvent> eventHistory;
    private static @Reflective @Getter Map<UserPlatform, List<User>> viewers;
    private static @Reflective @Getter Map<UserPlatform, UserUpdateEvent> userStates;
    private static @Reflective @Getter Map<UserPlatform, StreamStatusEvent> streamStates;

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

    public static List<UserPlatform> getSignedInPlatforms() {
        return new LinkedList<>(userStates.keySet());
    }

    public static boolean isMultiUserMode() {
        return userStates.size() > 1;
    }

    public static boolean isSignedOut() {
        return userStates.size() == 0;
    }

    /**
     * It's important to note that this is really only useful outside of multi-user
     * mode.
     * 
     * @throws IndexOutOfBoundsException if no user is signed in.
     */
    public static UserPlatform getFirstSignedInPlatform() {
        return getSignedInPlatforms().get(0);
    }

}
