package co.casterlabs.caffeinated.bootstrap;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.bootstrap.theming.ThemeableJFrame;
import co.casterlabs.caffeinated.bootstrap.theming.ThemeableJFrame.UnimplementedThemeableFrame;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public abstract class NativeSystemProvider {
    private static boolean INITIALIZED = false;

    private static LafManager lafManager = null;

    @Deprecated
    public static void initialize(@Nullable LafManager lafManager) {
        assert !INITIALIZED : "NativeSystemProvider has already been initialized.";

        NativeSystemProvider.lafManager = lafManager;
        INITIALIZED = true;
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

}
