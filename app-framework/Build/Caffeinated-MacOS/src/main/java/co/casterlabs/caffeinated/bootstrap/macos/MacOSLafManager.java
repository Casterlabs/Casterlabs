package co.casterlabs.caffeinated.bootstrap.macos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.lang.reflect.Method;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;

import co.casterlabs.caffeinated.bootstrap.theming.LafManager;
import co.casterlabs.caffeinated.bootstrap.theming.ThemeableJFrame;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class MacOSLafManager extends LafManager {
    private static final int NSWINDOW_STYLEMASK_FULL_WINDOW_CONTENT = 1 << 14;
    private static final int NSWINDOW_STYLEMASK_TRANSPARENT_TITLE_BAR = 1 << 18;

    // These colors were obtained by screenshotting an app and grabbing the color
    // values. (They might be *slightly* off)
    private static final Color LIGHT_TITLEBAR_BACKGROUND_ENABLED = new Color(239, 240, 239);
    private static final Color LIGHT_TITLEBAR_FOREGROUND_ENABLED = new Color(71, 72, 71);
    private static final Color LIGHT_TITLEBAR_BORDER_ENABLED = new Color(217, 217, 217);

    private static final Color DARK_TITLEBAR_BACKGROUND_ENABLED = new Color(55, 57, 57);
    private static final Color DARK_TITLEBAR_FOREGROUND_ENABLED = new Color(179, 181, 181);
    private static final Color DARK_TITLEBAR_BORDER_ENABLED = new Color(0, 0, 0);

    @Override
    protected ThemeableJFrame getFrame0() throws Exception {
        return new MacOSThemeableJFrame();
    }

    private static class MacOSThemeableJFrame extends ThemeableJFrame {
        private static final long serialVersionUID = ThemeableJFrame.serialVersionUID;

        private boolean isFirstTimeSetup = true;
        private JLabel titleBarLabel = new JLabel("", SwingConstants.CENTER);
        private JPanel titleBar;

        public MacOSThemeableJFrame() {
            // We set the title to BLANK because we cannot hide the
            // native NSWindow titlebar without some Obj-C code.
            // (Setting NSWINDOW_STYLEMASK_TITLED to false does NOT work.)
            // This also doesn't affect us since MacOS uses the executable's
            // app manifest for the app title and not the window's name.
            this.setTitle("");
        }

        @Override
        public void setTitle(String newTitle) {
            this.titleBarLabel.setText(newTitle);
            this.updateTitleBar();
        }

        private void updateTitleBar() {
            if (this.isVisible()) {
                this.tryFirstTimeSetup();

                if (this.titleBar != null) {
                    boolean dark = ThemeableJFrame.isDarkModeEnabled();

                    Color backgroundColor = dark ? DARK_TITLEBAR_BACKGROUND_ENABLED : LIGHT_TITLEBAR_BACKGROUND_ENABLED;
                    Color foregroundColor = dark ? DARK_TITLEBAR_FOREGROUND_ENABLED : LIGHT_TITLEBAR_FOREGROUND_ENABLED;
                    Color borderColor = dark ? DARK_TITLEBAR_BORDER_ENABLED : LIGHT_TITLEBAR_BORDER_ENABLED;

                    this.getContentPane().setBackground(dark ? Color.GRAY : Color.WHITE);
                    this.getContentPane().setForeground(dark ? Color.WHITE : Color.BLACK);

                    this.titleBarLabel.setForeground(foregroundColor);
                    this.titleBar.setForeground(foregroundColor);
                    this.titleBar.setBackground(backgroundColor);
                    this.titleBar.setBorder(new MatteBorder(0, 0, 1, 0, borderColor));

                    // Re-render.
                    SwingUtilities.updateComponentTreeUI(this);
                }
            }
        }

        @Override
        protected void setDarkMode0() throws Exception {
            this.updateTitleBar();
        }

        private void tryFirstTimeSetup() {
            if (this.isFirstTimeSetup) {
                this.isFirstTimeSetup = false;

                try {
                    @SuppressWarnings("deprecation")
                    Object peer = this.getPeer();
                    Object platformWindow = ReflectionLib.invokeMethod(peer, "getPlatformWindow");

                    boolean isValidPeer = Class.forName("sun.lwawt.LWWindowPeer").isAssignableFrom(peer.getClass()) &&
                        Class.forName("sun.lwawt.macosx.CPlatformWindow").isAssignableFrom(platformWindow.getClass());

                    if (isValidPeer) {
                        // This is the code that we're mimicking
                        /*
                        CPlatformWindow platformWindow = (CPlatformWindow) ((LWWindowPeer) peer).getPlatformWindow();
                        
                        platformWindow.setStyleBits(NSWINDOW_STYLEMASK_FULL_WINDOW_CONTENT | NSWINDOW_STYLEMASK_TRANSPARENT_TITLE_BAR, true);
                         */

                        // Grab the method, setAccessible, and invoke.
                        Method setStyleBits = platformWindow.getClass().getDeclaredMethod("setStyleBits", int.class, boolean.class);
                        setStyleBits.setAccessible(true);
                        setStyleBits.invoke(platformWindow, NSWINDOW_STYLEMASK_FULL_WINDOW_CONTENT | NSWINDOW_STYLEMASK_TRANSPARENT_TITLE_BAR, true);

                        // Make the titlebar and add it.
                        this.titleBarLabel.setFont(this.titleBarLabel.getFont().deriveFont(Font.BOLD));

                        this.titleBar = new JPanel();
                        this.titleBar.setLayout(new BorderLayout(0, 0));
                        this.titleBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 29));

                        this.titleBar.add(this.titleBarLabel, BorderLayout.CENTER);

                        this.getContentPane().add(this.titleBar, BorderLayout.NORTH);
                    }
                } catch (Exception e) {
                    FastLogger.logException(e);
                }
            }
        }

    }

}
