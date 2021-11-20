package co.casterlabs.caffeinated.bootstrap.ui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

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

        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tryClose();
            }
        });

        this.frame.setLayout(new BorderLayout(0, 0));

        try {
            URL iconUrl = FileUtil.loadResourceAsUrl("assets/logo/casterlabs.png");

            if (iconUrl != null) {
                FastLogger.logStatic(LogLevel.DEBUG, "Set app icon.");
                ImageIcon img = new ImageIcon(iconUrl);
                this.frame.setIconImage(img.getImage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.frame.setResizable(true);
        this.frame.setSize(800, 600);
//      this.frame.setUndecorated(true);

        this.cefPanel = new JPanel();
        this.cefPanel.setLayout(new BorderLayout(0, 0));
        this.frame.getContentPane().add(this.cefPanel, BorderLayout.CENTER);

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
