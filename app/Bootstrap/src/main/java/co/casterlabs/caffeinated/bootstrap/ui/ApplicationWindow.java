package co.casterlabs.caffeinated.bootstrap.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;

import javax.swing.JFrame;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.window.WindowPreferences;
import co.casterlabs.caffeinated.bootstrap.NativeSystem;
import co.casterlabs.caffeinated.webview.WebviewLifeCycleListener;
import co.casterlabs.caffeinated.window.theming.ThemeableJFrame;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.consoleutil.ConsoleUtil;

@Getter
public class ApplicationWindow {
    private ThemeableJFrame frame;
    private WebviewLifeCycleListener listener;

    private boolean hasFocus = false;
    private String icon;
    private String title;

    @SuppressWarnings("deprecation")
    private Unsafe_WindowState windowState = CaffeinatedApp.getInstance().getWindowState().unsafe;

    public ApplicationWindow(@NonNull WebviewLifeCycleListener listener, @NonNull Component webviewComponent) {
        this.listener = listener;
        this.frame = NativeSystem.getFrame();

        // Window stuff.
        PreferenceFile<WindowPreferences> preferenceFile = CaffeinatedApp.getInstance().getWindowPreferences();
        WindowPreferences windowPreferences = preferenceFile.get();

        this.updateBridgeData();

        this.frame.getContentPane().add(webviewComponent, BorderLayout.CENTER);
    }

    public boolean isMaximized() {
        return (this.frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
    }

    public void minmax() {
        // Toggles maximization
        this.frame.setExtendedState(this.frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
        this.frame.setTitle(this.title);
        this.updateBridgeData();
    }

    public void updateBridgeData() {
        this.windowState.title(this.title);
        this.windowState.icon(this.icon);
        this.windowState.maximized(this.isMaximized());
        this.windowState.platform(ConsoleUtil.getPlatform().name());
        this.windowState.hasFocus(this.hasFocus);
        this.windowState.update();
    }

    public void dispose() {
        this.frame.dispose();
    }

    public void toFront() {
        this.frame.setState(this.frame.getState() & ~JFrame.ICONIFIED); // State WITHOUT iconified flag.
        this.frame.toFront();
    }

    public void translate(int moveX, int moveY) {
        Point pos = this.frame.getLocation();

        pos.x -= moveX;
        pos.y -= moveY;

        this.frame.setLocation(pos);
    }

    public void minimize() {
        this.frame.setState(this.frame.getState() | JFrame.ICONIFIED);
    }

}
