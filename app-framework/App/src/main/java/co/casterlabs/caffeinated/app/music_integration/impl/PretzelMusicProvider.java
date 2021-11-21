package co.casterlabs.caffeinated.app.music_integration.impl;

import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.app.music_integration.MusicProvider;
import co.casterlabs.caffeinated.app.music_integration.impl.PretzelMusicProvider.PretzelSettings;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.NonNull;

public class PretzelMusicProvider extends MusicProvider<PretzelSettings> {

    public PretzelMusicProvider(@NonNull MusicIntegration musicIntegration) {
        super("Pretzel", "pretzel", PretzelSettings.class);
        musicIntegration.getProviders().put(this.getServiceId(), this);
    }

    @Override
    protected void onSettingsUpdate() {
        if (!this.isEnabled()) {
            this.setPlaybackStateInactive();
        }
    }

    public boolean isEnabled() {
        return this.getSettings().enabled;
    }

    @JsonClass(exposeAll = true)
    public static class PretzelSettings {
        private boolean enabled;

    }

}
