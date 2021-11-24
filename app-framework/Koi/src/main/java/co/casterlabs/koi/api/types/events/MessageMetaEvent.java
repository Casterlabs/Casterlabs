package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class MessageMetaEvent extends Event {
    private String id;

    @JsonField("is_visible")
    private boolean visible = true;

    private int upvotes;

    @Override
    public EventType getType() {
        return EventType.META;
    }

}
