package co.casterlabs.caffeinated.app.music_integration;

import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.music_integration.impl.PretzelMusicProvider;
import co.casterlabs.caffeinated.app.music_integration.impl.SpotifyMusicProvider;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;

@Getter
public class MusicIntegration {
    private MusicIntegration activePlayback;

    private Map<String, MusicProvider<?>> providers = new HashMap<>();

    public void init() {
        this.updateBridgeData(); // Populate

        // Register the providers
        new PretzelMusicProvider(this);
        new SpotifyMusicProvider(this);

        // Load their settings
        PreferenceFile<MusicIntegrationPreferences> prefs = CaffeinatedApp.getInstance().getMusicIntegrationPreferences();
        JsonObject prefsSettings = prefs.get().getSettings();
        for (Map.Entry<String, MusicProvider<?>> entry : this.providers.entrySet()) {
            MusicProvider<?> provider = entry.getValue();
            String providerId = entry.getKey();

            // Inefficient, but whatever.
            if (!prefsSettings.containsKey(providerId)) {
                prefsSettings.put(providerId, new JsonObject());
                prefs.save();
            }

            try {
                Object settings = Rson.DEFAULT.fromJson(
                    prefsSettings.get(providerId),
                    provider.getSettingsClass()
                );

                provider.updateSettings(settings);
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateBridgeData() {
        JsonArray musicServices = new JsonArray();

//        for (AuthInstance inst : this.authInstances.values()) {
//            musicServices.add( ... );
//        }

        JsonObject bridgeData = new JsonObject()
            .put("activePlayback", Rson.DEFAULT.toJson(this.activePlayback))
            .put("musicServices", musicServices);

        CaffeinatedApp.getInstance().getBridge().getQueryData().put("music", bridgeData);
    }

}
