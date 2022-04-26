package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonArray;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class CatchupEvent extends KoiEvent {
    private JsonArray events;
    private @Setter boolean isFresh;

    @Override
    public KoiEventType getType() {
        return KoiEventType.CATCHUP;
    }

}
