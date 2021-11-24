package co.casterlabs.koi.api.types.events;

import java.util.List;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class ViewerListEvent extends Event {
    private List<User> viewers;

    @Override
    public EventType getType() {
        return EventType.VIEWER_LIST;
    }

}
