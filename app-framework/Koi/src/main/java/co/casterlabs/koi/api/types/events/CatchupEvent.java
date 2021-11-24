package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonArray;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class CatchupEvent extends Event {
    private JsonArray events;

    @Override
    public EventType getType() {
        return EventType.CATCHUP;
    }

}
