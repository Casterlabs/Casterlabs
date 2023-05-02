package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.SimpleProfile;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class LikeEvent extends KoiEvent {
    private @Getter SimpleProfile streamer;

    @JsonField("total_likes")
    private long totalLikes;

    @Override
    public KoiEventType getType() {
        return KoiEventType.LIKE;
    }

}
