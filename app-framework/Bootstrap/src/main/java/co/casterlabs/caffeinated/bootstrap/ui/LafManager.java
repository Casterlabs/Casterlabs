package co.casterlabs.caffeinated.bootstrap.ui;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.bootstrap.ui.ThemeableJFrame.UnimplementedThemeableFrame;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public abstract class LafManager {
    private static LafManager INSTANCE = null;
    private static boolean INITIALIZED = false;

    @Deprecated
    public static void initialize(@Nullable LafManager inst) {
        assert !INITIALIZED : "LafManager has already been initialized.";

        INSTANCE = inst;
        INITIALIZED = true;
    }

    public static ThemeableJFrame getFrame() {
        assert INITIALIZED : "Caffeinated was not started with a platform bootstrap.";

        if (INSTANCE != null) {
            try {
                // Platform specific.
                ThemeableJFrame result = INSTANCE.getFrame0();

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

    protected abstract ThemeableJFrame getFrame0() throws Exception;

}
