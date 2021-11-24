package co.casterlabs.koi.api.types.events;

import java.util.List;
import java.util.Map;

import co.casterlabs.koi.api.types.ExternalEmote;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class ChatEvent extends Event {
    private Map<String, String> emotes;
    private List<Mention> mentions;
    private List<String> links;
    private User sender;
    private String message;
    private String id;
    private Map<String, Map<String, ExternalEmote>> externalEmotes;

    private int upvotes = 0;

    @Override
    public EventType getType() {
        return EventType.CHAT;
    }

    @Getter
    @ToString
    @JsonClass(exposeAll = true)
    public static class Mention {
        private String target;
        private User mentioned;

    }

}
