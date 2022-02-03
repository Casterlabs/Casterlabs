package co.casterlabs.caffeinated.bootstrap;

import javax.swing.UIManager;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.music_integration.InternalMusicProvider;
import co.casterlabs.caffeinated.webview.Webview;
import co.casterlabs.caffeinated.webview.WebviewFactory;
import co.casterlabs.caffeinated.window.theming.ThemeableJFrame;
import co.casterlabs.caffeinated.window.theming.ThemeableJFrame.UnimplementedThemeableFrame;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public abstract class NativeSystem {
    private static boolean INITIALIZED = false;

    private static LafManager lafManager = null;
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
    public static void initialize(boolean startedOnFirstThread, @Nullable LafManager lafManager, @Nullable SystemPlaybackMusicProvider systemPlaybackMusicProvider, @NonNull WebviewFactory webviewFactory) {
        assert !INITIALIZED : "NativeSystemProvider has already been initialized.";
        INITIALIZED = true;

        awtSupported = !startedOnFirstThread; // AWT will not work on the first thread.
        System.setProperty("awt.supported", String.valueOf(awtSupported));

        NativeSystem.lafManager = lafManager;
        NativeSystem.systemPlaybackMusicProvider = systemPlaybackMusicProvider;

        // We set it here so we guarantee it gets set.
        ReflectionLib.setStaticValue(Webview.class, "webviewFactory", webviewFactory);

        ThemeableJFrame.FACTORY = NativeSystem::getFrame;
    }

    public static ThemeableJFrame getFrame() {
        assert INITIALIZED : "Caffeinated was not started with a platform bootstrap.";

        if (lafManager != null) {
            try {
                // Platform specific.
                ThemeableJFrame result = lafManager.getFrame0();

                if (result == null) {
                    FastLogger.logStatic(LogLevel.WARNING, "Unable to initialize a prettier window frame, ignoring.");
                } else {
                    return result;
                }
            } catch (Exception e) {
                FastLogger.logStatic(LogLevel.WARNING, "Unable to initialize a prettier window frame, ignoring.");
                FastLogger.logStatic(LogLevel.DEBUG, e);
            }
        }

        return new UnimplementedThemeableFrame();
    }

    public static interface LafManager {

        public ThemeableJFrame getFrame0() throws Exception;

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
