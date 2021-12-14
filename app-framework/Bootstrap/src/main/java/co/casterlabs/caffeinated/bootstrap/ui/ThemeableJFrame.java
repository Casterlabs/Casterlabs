package co.casterlabs.caffeinated.bootstrap.ui;

import javax.swing.JFrame;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class ThemeableJFrame extends JFrame {
    protected static final long serialVersionUID = -1551229002462222344L;

    private boolean hasAlreadyLoggedError = false;

    protected boolean darkModeEnabled = false;

    public final void setDarkMode(boolean enabled) {
        this.darkModeEnabled = enabled;

        if (this.isVisible()) {
            this.setDarkMode();
        }
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

        if (visible) {
            this.setDarkMode();
        }
    }

    private void setDarkMode() {
        try {
            this.setDarkMode0();
        } catch (Exception e) {
            if (!this.hasAlreadyLoggedError) {
                this.hasAlreadyLoggedError = true;
                FastLogger.logException(e);
            }
        }
    }

    protected abstract void setDarkMode0() throws Exception;

    public static class UnimplementedThemeableFrame extends ThemeableJFrame {
        private static final long serialVersionUID = ThemeableJFrame.serialVersionUID;

        @Override
        protected void setDarkMode0() {}

    }

}
