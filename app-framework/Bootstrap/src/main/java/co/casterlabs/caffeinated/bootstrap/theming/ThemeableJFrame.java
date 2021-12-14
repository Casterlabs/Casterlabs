package co.casterlabs.caffeinated.bootstrap.theming;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class ThemeableJFrame extends JFrame {
    protected static final long serialVersionUID = -1551229002462222344L;
    private static List<WeakReference<ThemeableJFrame>> frames = new ArrayList<>();
    private static @Getter boolean darkModeEnabled = false;

    private WeakReference<ThemeableJFrame> _$ = new WeakReference<>(this);
    private boolean hasAlreadyLoggedError = false;

    public ThemeableJFrame() {
        frames.add(this._$);
    }

    /**
     * Used to trick some platforms into re-rendering the frame.
     */
    protected final void flashVisibility() {
        super.setVisible(false);
        super.setVisible(true);
    }

    @Override
    public final void setVisible(boolean visible) {
        super.setVisible(visible);
        this.updateDarkMode();
    }

    private void updateDarkMode() {
        if (this.isVisible()) {
            try {
                this.setDarkMode0();
            } catch (Exception e) {
                if (!this.hasAlreadyLoggedError) {
                    this.hasAlreadyLoggedError = true;
                    FastLogger.logException(e);
                }
            }
        }
    }

    protected abstract void setDarkMode0() throws Exception;

    @Override
    protected void finalize() {
        frames.remove(this._$);
    }

    public static class UnimplementedThemeableFrame extends ThemeableJFrame {
        private static final long serialVersionUID = ThemeableJFrame.serialVersionUID;

        @Override
        protected void setDarkMode0() {}

    }

    public static void setDarkMode(boolean enabled) {
        darkModeEnabled = enabled;

        for (WeakReference<ThemeableJFrame> framePtr : frames) {
            framePtr.get().updateDarkMode();
        }

    }

}
