package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class ViewerLeaveEvent extends Event {
    private User viewer;

    @Override
    public EventType getType() {
        return EventType.VIEWER_LEAVE;
    }

}
