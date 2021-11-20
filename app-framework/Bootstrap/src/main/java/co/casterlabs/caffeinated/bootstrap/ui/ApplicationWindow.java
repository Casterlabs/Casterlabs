package co.casterlabs.caffeinated.bootstrap.ui;

import java.awt.BorderLayout;
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

import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.preferences.WindowPreferences;
import co.casterlabs.caffeinated.app.ui.UIPreferences;
import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@Getter
public class ApplicationWindow {
    private JFrame frame;
    private JPanel cefPanel;
    private UILifeCycleListener listener;

    private boolean disposed = false;

    static {
        LafManager.setupLAF();
    }

    public ApplicationWindow(UILifeCycleListener listener) {
        this.listener = listener;
        this.frame = new JFrame();

        PreferenceFile<WindowPreferences> preferenceFile = Bootstrap.getApp().getWindowPreferences();
        WindowPreferences windowPreferences = preferenceFile.get();

        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                windowPreferences.setStateFlags(e.getNewState());
                preferenceFile.save();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                tryClose();
            }
        });

        Timer saveTimer = new Timer(100, (e) -> {
            preferenceFile.save();
        });
        saveTimer.setRepeats(false);
        this.frame.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {

                if ((frame.getState() & JFrame.MAXIMIZED_BOTH) == 0) {
                    windowPreferences.setWidth(frame.getWidth());
                    windowPreferences.setHeight(frame.getHeight());
                    saveTimer.restart();
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if ((frame.getState() & JFrame.MAXIMIZED_BOTH) == 0) {
                    windowPreferences.setX(frame.getX());
                    windowPreferences.setY(frame.getY());
                    preferenceFile.save();
                    saveTimer.restart();
                }
            }
        });

        this.frame.setLayout(new BorderLayout(0, 0));

        this.updateAppIcon(Bootstrap.getApp().getUiPreferences());
        Bootstrap.getApp().getUiPreferences().addSaveListener(this::updateAppIcon);

        this.frame.setSize(windowPreferences.getWidth(), windowPreferences.getHeight());
        this.frame.setLocation(windowPreferences.getX(), windowPreferences.getY());
        this.frame.setState(windowPreferences.getStateFlags());
        this.frame.setResizable(true);
//      this.frame.setUndecorated(true);

        this.cefPanel = new JPanel();
        this.cefPanel.setLayout(new BorderLayout(0, 0));
        this.frame.getContentPane().add(this.cefPanel, BorderLayout.CENTER);

    }

    private void updateAppIcon(PreferenceFile<UIPreferences> uiPreferences) {
        try {
            String icon = uiPreferences.get().getIcon();
            URL iconUrl = FileUtil.loadResourceAsUrl(String.format("assets/logo/%s.png", icon));

            if (iconUrl != null) {
                FastLogger.logStatic(LogLevel.DEBUG, "Set app icon to %s.", icon);
                ImageIcon img = new ImageIcon(iconUrl);
                this.frame.setIconImage(img.getImage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryClose() {
        new Thread(() -> {
            if (!this.disposed && this.listener.onCloseAttempt()) {
                this.disposed = true;
                this.frame.dispose();
                ApplicationUI.getDevtools().destroy();
                FastLoggingFramework.close(); // Faster shutdown.
            }
        }).start();
    }

}
