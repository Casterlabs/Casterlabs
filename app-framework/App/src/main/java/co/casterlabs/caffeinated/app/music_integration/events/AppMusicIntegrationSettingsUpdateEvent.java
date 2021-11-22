package co.casterlabs.caffeinated.app.music_integration.events;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

@Getter
@JsonClass(exposeAll = true)
public class AppMusicIntegrationSettingsUpdateEvent extends AbstractCancellableEvent<AppMusicIntegrationEventType> {
    private String platform;
    private JsonObject settings;

    public AppMusicIntegrationSettingsUpdateEvent() {
        super(AppMusicIntegrationEventType.SETTINGS_UPDATE);
    }

}
