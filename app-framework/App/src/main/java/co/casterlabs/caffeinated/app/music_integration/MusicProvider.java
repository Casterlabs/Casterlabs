package co.casterlabs.caffeinated.app.music_integration;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@JsonClass(exposeAll = true)
public abstract class MusicProvider<T> {
    private final String serviceName;
    private final String serviceId;
    private final Class<?> settingsClass;

    private MusicPlaybackState playbackState = MusicPlaybackState.INACTIVE;
    private MusicTrack currentTrack = null;

    private T settings;

    @SuppressWarnings("unchecked")
    public void updateSettings(@NonNull Object settings) {
        this.settings = (T) settings;
        this.onSettingsUpdate();
        CaffeinatedApp.getInstance().getMusicIntegration().updateBridgeData();
    }

    protected abstract void onSettingsUpdate();

    protected void setPlaybackStateInactive() {
        this.playbackState = MusicPlaybackState.INACTIVE;
        this.currentTrack = null;
        CaffeinatedApp.getInstance().getMusicIntegration().updateBridgeData();
    }

    protected void setPlaying(@NonNull MusicTrack track) {
        this.playbackState = MusicPlaybackState.PLAYING;
        this.currentTrack = track;
        CaffeinatedApp.getInstance().getMusicIntegration().updateBridgeData();
    }

    protected void setPaused() {
        this.playbackState = MusicPlaybackState.PAUSED;
        CaffeinatedApp.getInstance().getMusicIntegration().updateBridgeData();
    }

}
