package co.casterlabs.caffeinated.window.theming;

import java.awt.BorderLayout;
import java.awt.Image;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import co.casterlabs.caffeinated.util.Producer;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class ThemeableJFrame extends JFrame {
    protected static final long serialVersionUID = -1551229002462222344L;
    private static List<WeakReference<ThemeableJFrame>> frames = new ArrayList<>();
    private static @Getter boolean darkModeEnabled = false;

    public static Producer<ThemeableJFrame> FACTORY = () -> new UnimplementedThemeableFrame();

    private WeakReference<ThemeableJFrame> $ref = new WeakReference<>(this);
    private boolean hasAlreadyLoggedError = false;

    public ThemeableJFrame() {
        frames.add(this.$ref);
        this.setLayout(new BorderLayout(0, 0));
    }

    /**
     * Used to trick some platforms into re-rendering the frame.
     */
    protected final void flashVisibility() {
        List<Image> images = this.getIconImages();

        super.setVisible(false);
        super.setVisible(true);

        this.setIconImages(images);
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
        frames.remove(this.$ref);
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
