package co.casterlabs.caffeinated.app.music_integration;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.app.AppBridge;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.music_integration.events.AppMusicIntegrationEventType;
import co.casterlabs.caffeinated.app.music_integration.events.AppMusicIntegrationSettingsUpdateEvent;
import co.casterlabs.caffeinated.app.music_integration.events.AppMusicIntegrationSignoutEvent;
import co.casterlabs.caffeinated.app.music_integration.impl.PretzelMusicProvider;
import co.casterlabs.caffeinated.app.music_integration.impl.SpotifyMusicProvider;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;

@Getter
public class MusicIntegration {
    private static EventHandler<AppMusicIntegrationEventType> handler = new EventHandler<>();

    private MusicProvider<?> activePlayback;

    private Map<String, MusicProvider<?>> providers = new HashMap<>();

    private boolean loaded = false;

    public MusicIntegration() {
        handler.register(this);
    }

    public void init() {
        this.updateBridgeData(); // Populate

        // Register the providers (in order of preference)
        new SpotifyMusicProvider(this);
        new PretzelMusicProvider(this);

        // Load their settings
        PreferenceFile<MusicIntegrationPreferences> prefs = CaffeinatedApp.getInstance().getMusicIntegrationPreferences();
        JsonObject prefsSettings = prefs.get().getSettings();
        for (Map.Entry<String, MusicProvider<?>> entry : this.providers.entrySet()) {
            MusicProvider<?> provider = entry.getValue();
            String providerId = entry.getKey();

            // Doesn't matter if it's null, we check for that inside of
            // MusicProvider#updateSettingsFromJson
            provider.updateSettingsFromJson(prefsSettings.get(providerId));
        }

        this.loaded = true;
        this.save();
    }

    public void save() {
        if (this.loaded) {
            PreferenceFile<MusicIntegrationPreferences> prefs = CaffeinatedApp.getInstance().getMusicIntegrationPreferences();
            JsonObject prefsSettings = prefs.get().getSettings();

            for (MusicProvider<?> provider : this.providers.values()) {
                prefsSettings.put(provider.getServiceId(), Rson.DEFAULT.toJson(provider.getSettings()));
            }

            prefs.save();

            this.updateBridgeData();
        }
    }

    @EventListener
    public void onMusicIntegrationSignoutEvent(AppMusicIntegrationSignoutEvent event) {
        this.providers.get(event.getPlatform()).signout();
    }

    @EventListener
    public void onMusicIntegrationSettingsUpdateEvent(AppMusicIntegrationSettingsUpdateEvent event) {
        this.providers.get(event.getPlatform()).updateSettingsFromJson(event.getSettings());
    }

    public void updateBridgeData() {
        JsonObject musicServices = new JsonObject();

        MusicProvider<?> pausedTrack = null;
        MusicProvider<?> playingTrack = null;

        for (MusicProvider<?> provider : this.providers.values()) {
            musicServices.put(provider.getServiceId(), Rson.DEFAULT.toJson(provider));

            if ((pausedTrack == null) && (provider.getPlaybackState() == MusicPlaybackState.PAUSED)) {
                pausedTrack = provider;
            } else if ((playingTrack == null) && (provider.getPlaybackState() == MusicPlaybackState.PLAYING)) {
                playingTrack = provider;
            }
        }

        if (playingTrack != null) {
            this.activePlayback = playingTrack;
        } else if (pausedTrack != null) {
            this.activePlayback = pausedTrack;
        } else {
            this.activePlayback = null;
        }

        JsonObject bridgeData = new JsonObject()
            .put("activePlayback", Rson.DEFAULT.toJson(this.activePlayback))
            .put("musicServices", musicServices);

        AppBridge bridge = CaffeinatedApp.getInstance().getBridge();

        bridge.getQueryData().put("music", bridgeData);
        bridge.emit("music:update", bridgeData);
    }

    public static void invokeEvent(JsonObject data, String nestedType) throws InvocationTargetException, JsonParseException {
        handler.call(
            Rson.DEFAULT.fromJson(
                data,
                AppMusicIntegrationEventType
                    .valueOf(nestedType)
                    .getEventClass()
            )
        );
    }

}
