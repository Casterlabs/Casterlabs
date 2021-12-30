package co.casterlabs.caffeinated.app.music_integration;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.pluginsdk.music.MusicPlaybackState;
import co.casterlabs.caffeinated.pluginsdk.music.MusicProvider;
import co.casterlabs.caffeinated.pluginsdk.music.MusicTrack;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@JsonClass(exposeAll = true)
public abstract class InternalMusicProvider<T> implements MusicProvider {
    private final String serviceName;
    private final String serviceId;
    private final Class<T> settingsClass;

    private boolean isSignedIn;
    private String accountName;
    private String accountLink;

    private MusicPlaybackState playbackState = MusicPlaybackState.INACTIVE;
    private MusicTrack currentTrack = null;

    private T settings;

    @SuppressWarnings("unchecked")
    protected void updateSettings(@NonNull Object settings) {
        this.settings = (T) settings;
        this.onSettingsUpdate();
        CaffeinatedApp.getInstance().getMusicIntegration().save(); // Auto updates bridge data.
    }

    public void updateSettingsFromJson(@Nullable JsonElement settings) {
        try {
            if (settings == null) {
                this.updateSettings(this.settingsClass.newInstance());
            } else {
                this.updateSettings(Rson.DEFAULT.fromJson(settings, this.settingsClass));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void onSettingsUpdate();

    protected void setAccountData(boolean isSignedIn, String accountName, String accountLink) {
        if (isSignedIn) {
            this.isSignedIn = true;
            this.accountName = accountName;
            this.accountLink = accountLink;
            CaffeinatedApp.getInstance().getMusicIntegration().updateBridgeData();
        } else {
            this.isSignedIn = false;
            this.accountName = null;
            this.accountLink = null;
            this.setPlaybackStateInactive(); // Will autoupdate bridge.
        }
    }

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

    protected void setPaused(@NonNull MusicTrack track) {
        this.playbackState = MusicPlaybackState.PAUSED;
        this.currentTrack = track;
        CaffeinatedApp.getInstance().getMusicIntegration().updateBridgeData();
    }

    protected void makePaused() {
        if (this.currentTrack != null) {
            this.playbackState = MusicPlaybackState.PAUSED;
            CaffeinatedApp.getInstance().getMusicIntegration().updateBridgeData();
        }
    }

    public abstract void signout();

}
