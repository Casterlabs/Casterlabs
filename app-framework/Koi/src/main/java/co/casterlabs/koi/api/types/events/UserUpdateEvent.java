package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class UserUpdateEvent extends Event {
    private String timestamp;

    @Override
    public EventType getType() {
        return EventType.USER_UPDATE;
    }

}
