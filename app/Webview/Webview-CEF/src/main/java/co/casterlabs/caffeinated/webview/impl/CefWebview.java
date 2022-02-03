package co.casterlabs.caffeinated.webview.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandlerAdapter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefRequest.TransitionType;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.webview.Webview;
import co.casterlabs.caffeinated.webview.WebviewFactory;
import co.casterlabs.caffeinated.webview.WebviewFileUtil;
import co.casterlabs.caffeinated.webview.bridge.WebviewBridge;
import co.casterlabs.caffeinated.window.theming.ThemeableJFrame;
import lombok.NonNull;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.consoleutil.platform.JavaPlatform;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class CefWebview extends Webview {

    public static final WebviewFactory FACTORY = new WebviewFactory() {
        @Override
        public @Nullable Webview produce() throws Exception {

            return new CefWebview();
        }

        @Override
        public boolean useNuclearOption() {
            // The scheme only works on Windows for some dumb reason.
            return ConsoleUtil.getPlatform() != JavaPlatform.WINDOWS;
        }

        @Override
        protected void setIcon0(@NonNull String icon) {
            for (WeakReference<Webview> wv : webviews) {
                ((CefWebview) wv.get()).updateAppIcon(icon);
            }
        }
    };

    private static FastLogger logger = new FastLogger();
    private static boolean cefInitialized = false;

    private ThemeableJFrame frame;
    private JPanel cefPanel;

    private CefClient client;
    private CefJavascriptBridge bridge;

    private CefBrowser browser;
    private CefDevTools devtools;

    @Override
    protected void initialize0() throws Exception {
        // One-time setup.
        if (!cefInitialized) {
            cefInitialized = true;
            CefUtil.create(false /* I hate this. */, Webview.WEBVIEW_SCHEME, this.getSchemeHandler());
        }

        // Setup the panel
        this.cefPanel = new JPanel();
        this.cefPanel.setLayout(new BorderLayout(0, 0));

        // Create the frame.
        this.frame = ThemeableJFrame.FACTORY.produce();

        Timer saveTimer = new Timer(500, (e) -> {
            this.windowState.update();
        });
        saveTimer.setRepeats(false);

        this.updateAppIcon(WebviewFactory.getCurrentIcon());

        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.frame.setSize(this.windowState.getWidth(), this.windowState.getHeight());
        this.frame.setLocation(this.windowState.getX(), this.windowState.getY());
        this.frame.setResizable(true);
        this.frame.setMinimumSize(new Dimension(this.windowState.getMinWidth(), this.windowState.getMinHeight()));

        this.frame.add(this.cefPanel, BorderLayout.CENTER);

        this.frame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                CefWebview.this.windowState.setHasFocus(true);
                CefWebview.this.windowState.update();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                CefWebview.this.windowState.setHasFocus(false);
                CefWebview.this.windowState.update();
            }
        });

        this.frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!isMaximized()) {
                    CefWebview.this.windowState.setWidth(frame.getWidth());
                    CefWebview.this.windowState.setHeight(frame.getHeight());
                    saveTimer.restart();
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if (!isMaximized()) {
                    CefWebview.this.windowState.setX(frame.getX());
                    CefWebview.this.windowState.setY(frame.getY());
                    saveTimer.restart();
                }
            }
        });

        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                CefWebview.this.getLifeCycleListener().onMinimize();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                CefWebview.this.getLifeCycleListener().onCloseRequested();
            }
        });

        // Cef
        this.client = CefUtil.createCefClient();
        this.bridge = new CefJavascriptBridge(this.client);

        // Context menu
        this.client.addContextMenuHandler(new CefContextMenuHandlerAdapter() {
            // ID | Name
            // ---+-------------------------
            // 01 | Inspect Element
            // 02 | Reload
            //

            @Override
            public void onBeforeContextMenu(CefBrowser browser, CefFrame frame, CefContextMenuParams params, CefMenuModel model) {
                model.clear();

//                if ( Bootstrap.isDev() || Bootstrap.getInstance().isDevToolsEnabled()) {
                model.addItem(2, "Reload");

                model.addCheckItem(1, "Inspect Element");
                model.setChecked(1, devtools.isOpen());

//                    model.addSeparator();
//                    model.addItem(99, "Close This Popup");
//                }
            }

            @Override
            public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame, CefContextMenuParams params, int commandId, int eventFlags) {
                switch (commandId) {
                    case 1: {
                        devtools.toggle();
                        break;
                    }

                    case 2: {
                        browser.reloadIgnoreCache();
                        break;
                    }
                }
                return true;
            }
        });

        // Load handler
        logger.debug("Loadstate 0");
        this.getLifeCycleListener().onBrowserPreLoad();
        this.client.addLoadHandler(new CefLoadHandlerAdapter() {
            // 0 = about:blank (preload)
            // 1 = app://index (load)
            // 2 = ... (completely loaded)
            private int loadState = 0;

            @Override
            public void onLoadEnd(CefBrowser _browser, CefFrame _frame, int httpStatusCode) {
                if (this.loadState == 0) {
                    logger.debug("Loadstate 1");
                    this.loadState = 2;
                    logger.debug("Loadstate 2");
                }
            }

            @Override
            public void onLoadStart(CefBrowser _browser, CefFrame _frame, TransitionType transitionType) {
                if (browser == _browser) {
                    logger.info("Injected Bridge.");
                    bridge.injectBridgeScript(browser.getMainFrame());
                    bridge.attachValue(windowState.getBridge());
                }
            }
        });

        // Lifespan
        this.client.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public void onAfterCreated(CefBrowser _browser) {
                if (browser == _browser) {
                    logger.info("Created window.");
                    devtools = new CefDevTools(browser);
                }
            }
        });

        this.client.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onTitleChange(CefBrowser browser, String title) {
                new AsyncTask(() -> {
                    if ((title == null) ||
                        title.equals("null") ||
                        title.equals("undefined") ||
                        title.isEmpty() ||
                        getCurrentURL().contains(title)) {
                        frame.setTitle("Casterlabs Caffeinated");
                    } else {
                        frame.setTitle("Casterlabs Caffeinated - " + title);
                    }
                });
            }
        });
    }

    public boolean isMaximized() {
        return (this.frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
    }

    @Override
    public void loadURL(@Nullable String url) {
        if (url == null) {
            url = "about:blank";
        }

        this.browser.loadURL(url);
    }

    @Override
    public String getCurrentURL() {
        return this.browser.getURL();
    }

    @Override
    public void executeJavaScript(@NonNull String script) {
        this.browser.executeJavaScript(script, "app://app.local", 0);
    }

    @Override
    public WebviewBridge getBridge() {
        return this.bridge;
    }

    @Override
    public void open(@Nullable String url) {
        if (this.browser == null) {
            if (url == null) {
                url = "about:blank";
            }

            // Create browser
            this.browser = this.client.createBrowser(url, this.isOffScreenRenderingEnabled(), this.isTransparencyEnabled());

            // Add it to the JPanel.
            this.cefPanel.add(this.browser.getUIComponent(), BorderLayout.CENTER);
            this.frame.setVisible(true);

            // Notify
            this.getLifeCycleListener().onBrowserOpen();
        }
    }

    @Override
    public void close() {
        if (this.browser != null) {
            this.frame.setVisible(false);

            // Remove the frame
            this.cefPanel.removeAll();

            // Destroy the browser and devtools
            if (this.devtools != null) {
                this.devtools.close();
            }

            browser.close(false);
            browser = null;

            // Notify
            this.getLifeCycleListener().onBrowserClose();
        }
    }

    private void updateAppIcon(@Nullable String icon) {
        if (icon != null) {
            try {
                URL iconUrl = WebviewFileUtil.loadResourceAsUrl(String.format("assets/logo/%s.png", icon));

                if (iconUrl != null) {
                    ImageIcon img = new ImageIcon(iconUrl);
                    this.frame.setIconImage(img.getImage());

                    FastLogger.logStatic(LogLevel.DEBUG, "Set app icon to %s.", icon);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() {
        this.frame.dispose();
    }

    @Override
    public void focus() {
        this.frame.toFront();
    }

    @Override
    public boolean isOpen() {
        return this.frame.isVisible();
    }

}
