package co.casterlabs.caffeinated.bootstrap.macos;

import java.lang.reflect.Method;

import javax.swing.SwingUtilities;

import co.casterlabs.caffeinated.bootstrap.theming.LafManager;
import co.casterlabs.caffeinated.bootstrap.theming.ThemeableJFrame;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class MacOSLafManager extends LafManager {
    private static final int FULL_WINDOW_CONTENT = 1 << 14;
    private static final int TRANSPARENT_TITLE_BAR = 1 << 18;

    @Override
    protected ThemeableJFrame getFrame0() throws Exception {
        return new MacOSThemeableJFrame();
    }

    private static class MacOSThemeableJFrame extends ThemeableJFrame {
        private static final long serialVersionUID = ThemeableJFrame.serialVersionUID;

        @Override
        protected void setDarkMode0() throws Exception {
            @SuppressWarnings("deprecation")
            Object peer = this.getPeer();
            Object platformWindow = ReflectionLib.invokeMethod(peer, "getPlatformWindow");

            boolean isValidPeer = Class.forName("sun.lwawt.LWWindowPeer").isAssignableFrom(peer.getClass()) &&
                Class.forName("sun.lwawt.macosx.CPlatformWindow").isAssignableFrom(platformWindow.getClass());

            if (isValidPeer) {
                int MASK = FULL_WINDOW_CONTENT | TRANSPARENT_TITLE_BAR;

                // This is the code that we're mimicking
                /*
                CPlatformWindow platformWindow = (CPlatformWindow) ((LWWindowPeer) peer).getPlatformWindow();
                
                platformWindow.setStyleBits(MASK, true);
                 */

                // Grab the method, setAccessible, and invoke.
                Method setStyleBits = platformWindow.getClass().getDeclaredMethod("setStyleBits", int.class, boolean.class);
                setStyleBits.setAccessible(true);
                setStyleBits.invoke(platformWindow, MASK, true);

                // TODO make our own titlebar based on the theme and go ham.

                // Re-render.
                SwingUtilities.updateComponentTreeUI(this);
            }
        }

    }

}
