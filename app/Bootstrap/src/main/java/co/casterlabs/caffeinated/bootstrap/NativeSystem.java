package co.casterlabs.caffeinated.bootstrap;

import javax.swing.UIManager;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.music_integration.InternalMusicProvider;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.SneakyThrows;

public abstract class NativeSystem {
    private static boolean INITIALIZED = false;

    private static @Getter SystemPlaybackMusicProvider systemPlaybackMusicProvider = null;
    private static @Getter boolean awtSupported;

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    @Deprecated
    @SneakyThrows
    // Something being null = unsupported.
    public static void initialize(boolean startedOnFirstThread, @Nullable SystemPlaybackMusicProvider systemPlaybackMusicProvider) {
        assert !INITIALIZED : "NativeSystemProvider has already been initialized.";
        INITIALIZED = true;

        awtSupported = !startedOnFirstThread; // AWT will not work on the first thread.
        System.setProperty("awt.supported", String.valueOf(awtSupported));

        NativeSystem.systemPlaybackMusicProvider = systemPlaybackMusicProvider;
    }

    public static abstract class SystemPlaybackMusicProvider extends InternalMusicProvider<SystemPlaybackSettings> {

        public SystemPlaybackMusicProvider() {
            super("System", "system", SystemPlaybackSettings.class);
        }

        protected abstract void init();

        protected abstract void update();

        @Override
        protected void onSettingsUpdate() {
            if (this.isEnabled()) {
                this.update();
            } else {
                this.setPlaybackStateInactive();
            }
        }

        @Override
        public void signout() {} // NO-OP

        public boolean isEnabled() {
            return this.getSettings().enabled;
        }
    }

    @JsonClass(exposeAll = true)
    public static class SystemPlaybackSettings {
        private boolean enabled = false;

    }

}
