package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class ClearChatEvent extends Event {
    @JsonField("user_upid")
    private String userUPID;

    @JsonField("clear_type")
    private ClearChatType clearType;

    @Override
    public EventType getType() {
        return EventType.CLEARCHAT;
    }

    public static enum ClearChatType {
        ALL,
        USER;

    }

}
