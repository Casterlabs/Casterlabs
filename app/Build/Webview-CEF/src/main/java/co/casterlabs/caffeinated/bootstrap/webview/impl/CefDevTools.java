package co.casterlabs.caffeinated.bootstrap.webview.impl;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.cef.browser.CefBrowser;

public class CefDevTools {
    private CefBrowser browser;

    private CefBrowser devtools;
    private JFrame frame;

    public CefDevTools(CefBrowser browser) {
        this.browser = browser;
    }

    public boolean isOpen() {
        return this.frame != null;
    }

    public void summon() {
        if (!this.isOpen()) {
            this.frame = new JFrame();

            this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    close();
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

    public void close() {
        if (this.isOpen()) {
            this.frame.dispose();
            this.devtools.close(false);
            this.frame = null;
            this.devtools = null;
        }
    }

    public void toggle() {
        if (this.isOpen()) {
            this.close();
        } else {
            this.summon();
        }
    }

}
