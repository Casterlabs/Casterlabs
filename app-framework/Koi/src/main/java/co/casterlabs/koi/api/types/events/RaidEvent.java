package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class RaidEvent extends Event {
    private User host;
    private int viewers;

    @Override
    public EventType getType() {
        return EventType.RAID;
    }

}
