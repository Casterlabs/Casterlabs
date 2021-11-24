package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public abstract class Event {
    public User streamer;

    @JsonField("event_abilities")
    public EventAbilities abilities;

    public abstract EventType getType();

    @Getter
    @ToString
    @JsonClass(exposeAll = true)
    public static class EventAbilities {
        private boolean upvotable = false;
        private boolean deletable = false;

    }

}
