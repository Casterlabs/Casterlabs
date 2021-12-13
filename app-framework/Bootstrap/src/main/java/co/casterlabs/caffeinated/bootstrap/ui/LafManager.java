package co.casterlabs.caffeinated.bootstrap.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.lang.reflect.Method;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import lombok.NonNull;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class LafManager {

    public static void setupLaf() {
        try {
            // The default.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Platform specifics.
            switch (ConsoleUtil.getPlatform()) {
                case WINDOWS: {
                    setupWindows();
                    break;
                }

                default:
                    break;
            }
        } catch (Exception e) {
            FastLogger.logException(e);
        }
    }

    public static void frameInit(@NonNull JFrame frame) {
        try {
            // Platform specifics.
            switch (ConsoleUtil.getPlatform()) {
                case MAC: {
                    // We need to access properties of the JFrame directly.
                    setupMac(frame);
                    break;
                }

                default:
                    break;
            }
        } catch (Exception e) {
            FastLogger.logException(e);
        }
    }

    private static void setupWindows() throws Exception {
        // Only for Win10
        if (System.getProperty("os.name").equals("Windows 10")) {
            UIDefaults uiDefaults = UIManager.getDefaults();

            uiDefaults.put("activeCaption", new javax.swing.plaf.ColorUIResource(Color.GRAY));
            uiDefaults.put("activeCaptionText", new javax.swing.plaf.ColorUIResource(Color.WHITE));

            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            JFrame.setDefaultLookAndFeelDecorated(true);
            FastLogger.logStatic("Set Window10 Darkmode LAF");
        }
    }

    private static void setupMac(JFrame frame) throws Exception {
        // Save location and size.
        Point prevLoc = frame.getLocation();
        Dimension prevSize = frame.getSize();

        // Make it tiny and move it offscreen.
        frame.setSize(0, 0);
        frame.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);

        // Generate the window.
        frame.setVisible(true);
        frame.setVisible(false);

        try {
            final int FULL_WINDOW_CONTENT = 1 << 14;
            final int TRANSPARENT_TITLE_BAR = 1 << 18;

            @SuppressWarnings("deprecation")
            Object peer = frame.getPeer();
            Object platformWindow = ReflectionLib.invokeMethod(peer, "getPlatformWindow");

            if (peer.getClass().getCanonicalName().equals("sun.lwawt.LWWindowPeer") &&
                platformWindow.getClass().getCanonicalName().equals("sun.lwawt.macosx.CPlatformWindow")) {
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

                // Re-render
                SwingUtilities.updateComponentTreeUI(frame);
            } else {
                FastLogger.logStatic(LogLevel.WARNING, "Unable to initialize a prettier window frame, ignoring.");
            }
        } finally {
            // Move it back.
            frame.setLocation(prevLoc);
            frame.setSize(prevSize);
        }
    }

}
