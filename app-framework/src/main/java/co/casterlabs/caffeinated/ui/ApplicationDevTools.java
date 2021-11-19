package co.casterlabs.caffeinated.ui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.cef.browser.CefBrowser;

public class ApplicationDevTools {
    private CefBrowser browser;

    private CefBrowser devtools;
    private JFrame frame;

    public ApplicationDevTools(CefBrowser browser) {
        this.browser = browser;
    }

    public void summon() {
        if (this.frame == null) {
            this.frame = new JFrame();

            this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    destroy();
                }
            });

            this.frame.setUndecorated(false);
            this.frame.setResizable(true);
            this.frame.setSize(800, 600);
            this.frame.setTitle("DevTools");
            this.frame.setVisible(true);

            this.devtools = this.browser.getDevTools();
            this.frame.add(this.devtools.getUIComponent(), BorderLayout.CENTER);
        }
    }

    public void destroy() {
        if (this.frame != null) {
            this.frame.dispose();
            this.devtools.close(false);
            this.frame = null;
            this.devtools = null;
        }
    }

}
