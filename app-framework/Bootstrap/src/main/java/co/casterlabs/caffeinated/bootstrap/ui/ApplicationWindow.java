package co.casterlabs.caffeinated.bootstrap.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import co.casterlabs.caffeinated.app.AppBridge;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.preferences.WindowPreferences;
import co.casterlabs.caffeinated.app.ui.UIPreferences;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@Getter
public class ApplicationWindow {
    public static final boolean ENABLE_CUSTOM_TITLEBARS = System.getProperty("caffeinated.window.customtitlebar.enable", "").equals("true");

    private JFrame frame;
    private JPanel cefPanel;
    private UILifeCycleListener listener;

    private boolean hasFocus = false;
    private String icon;
    private String title;

    static {
        LafManager.setupLAF();
    }

    public ApplicationWindow(UILifeCycleListener listener) {
        this.listener = listener;
        this.frame = new JFrame();

        PreferenceFile<WindowPreferences> preferenceFile = CaffeinatedApp.getInstance().getWindowPreferences();
        WindowPreferences windowPreferences = preferenceFile.get();

        Timer saveTimer = new Timer(500, (e) -> {
            preferenceFile.save();
            this.updateBridgeData();
        });
        saveTimer.setRepeats(false);

        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                listener.onMinimize();
            }

            @Override
            public void windowStateChanged(WindowEvent e) {
                windowPreferences.setStateFlags(e.getNewState() & ~JFrame.ICONIFIED); // State WITHOUT iconified flag.
                saveTimer.restart();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                listener.onUICloseAttempt();
            }
        });

        this.frame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                hasFocus = true;
                updateBridgeData();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                hasFocus = false;
                updateBridgeData();
            }
        });

        this.frame.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                if (!isMaximized()) {
                    windowPreferences.setWidth(frame.getWidth());
                    windowPreferences.setHeight(frame.getHeight());
                    saveTimer.restart();
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if (!isMaximized()) {
                    windowPreferences.setX(frame.getX());
                    windowPreferences.setY(frame.getY());
                    saveTimer.restart();
                }
            }
        });

        this.frame.setLayout(new BorderLayout(0, 0));

        this.updateAppIcon(CaffeinatedApp.getInstance().getUiPreferences());
        CaffeinatedApp.getInstance().getUiPreferences().addSaveListener(this::updateAppIcon);

        this.frame.setSize(windowPreferences.getWidth(), windowPreferences.getHeight());
        this.frame.setLocation(windowPreferences.getX(), windowPreferences.getY());
        this.frame.setState(windowPreferences.getStateFlags());
        this.frame.setResizable(true);
        this.frame.setMinimumSize(new Dimension(800, 580));

        this.updateBridgeData();

        this.cefPanel = new JPanel();
        this.cefPanel.setLayout(new BorderLayout(0, 0));
        this.frame.getContentPane().add(this.cefPanel, BorderLayout.CENTER);

        if (ENABLE_CUSTOM_TITLEBARS) {
            // We implement our own title bar in html, as it can be easily restyled.
            switch (ConsoleUtil.getPlatform()) {
                case WINDOWS:
                    this.frame.setUndecorated(true);
                    this.cefPanel.setBorder(new LineBorder(Color.GRAY, 2));

                    new ComponentResizer()
                        .registerComponent(this.frame);

                    break;

                default:
                    break;

            }
        }
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
        new AsyncTask(() -> {
            AppBridge bridge = ApplicationUI.getBridge();

            if (bridge != null) {
                JsonObject bridgeData = new JsonObject()
                    .put("title", this.title)
                    .put("icon", this.icon)
                    .put("maximized", this.isMaximized())
                    .put("platform", ConsoleUtil.getPlatform().name())
                    .put("hasFocus", this.hasFocus)
                    .put("enableTitleBar", ENABLE_CUSTOM_TITLEBARS);

                bridge.getQueryData().put("window", bridgeData);
                bridge.emit("window:update", bridgeData);
            }
        });
    }

    private void updateAppIcon(PreferenceFile<UIPreferences> uiPreferences) {
        try {
            String icon = uiPreferences.get().getIcon();
            URL iconUrl = FileUtil.loadResourceAsUrl(String.format("assets/logo/%s.png", icon));

            if (iconUrl != null) {
                this.icon = icon;

                ImageIcon img = new ImageIcon(iconUrl);
                this.frame.setIconImage(img.getImage());

                this.updateBridgeData();

                FastLogger.logStatic(LogLevel.DEBUG, "Set app icon to %s.", this.icon);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
