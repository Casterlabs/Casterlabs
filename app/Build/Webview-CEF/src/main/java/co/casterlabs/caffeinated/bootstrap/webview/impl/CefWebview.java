package co.casterlabs.caffeinated.bootstrap.webview.impl;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandler;
import org.cef.handler.CefLifeSpanHandler;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.network.CefRequest.TransitionType;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.webview.AppWebview;
import co.casterlabs.caffeinated.bootstrap.webview.JavascriptBridge;
import co.casterlabs.caffeinated.util.Producer;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class CefWebview extends AppWebview {
    public static final Producer<AppWebview> FACTORY = () -> {
        return new CefWebview();
    };

    private static FastLogger logger = new FastLogger();
    private static boolean cefInitialized = false;

    private JPanel cefPanel;

    private CefClient client;
    private CefJavascriptBridge bridge;

    private CefBrowser browser;
    private CefDevTools devtools;

    @Override
    protected Component initialize0() {
        // One-time setup.
        if (!cefInitialized) {
            cefInitialized = true;
            CefUtil.create(AppWebview.WEBVIEW_SCHEME, AppWebview.getSchemeHandler());
        }

        // Setup the panel
        this.cefPanel = new JPanel();
        this.cefPanel.setLayout(new BorderLayout(0, 0));

        this.client = CefUtil.createCefClient();
        this.bridge = new CefJavascriptBridge(this.client);

        // Context menu
        this.client.addContextMenuHandler(new CefContextMenuHandler() {
            // ID | Name
            // ---+-------------------------
            // 01 | Inspect Element
            // 02 | Reload
            //

            @Override
            public void onBeforeContextMenu(CefBrowser browser, CefFrame frame, CefContextMenuParams params, CefMenuModel model) {
                model.clear();

                if (Bootstrap.isDev() || Bootstrap.getInstance().isDevToolsEnabled()) {
                    model.addItem(2, "Reload");

                    model.addCheckItem(1, "Inspect Element");
                    model.setChecked(1, devtools.isOpen());

                    model.addSeparator();
                    model.addItem(99, "Close This Popup");
                }
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

            @Override
            public void onContextMenuDismissed(CefBrowser browser, CefFrame frame) {}
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
                    getLifeCycleListener().onBrowserInitialLoad();
                    this.loadState = 2;
                    logger.debug("Loadstate 2");
                }
            }

            @Override
            public void onLoadStart(CefBrowser _browser, CefFrame _frame, TransitionType transitionType) {
                if (browser == _browser) {
                    logger.info("Injected Bridge.");
                    bridge.injectBridgeScript(browser.getMainFrame());
                }
            }

        });

        // Lifespan
        this.client.addLifeSpanHandler(new CefLifeSpanHandler() {

            @Override
            public boolean doClose(CefBrowser var1) {
                return false;
            }

            @Override
            public void onAfterCreated(CefBrowser _browser) {
                if (browser == _browser) {
                    logger.info("Created window.");
                    devtools = new CefDevTools(browser);
                }
            }

            @Override
            public void onAfterParentChanged(CefBrowser var1) {}

            @Override
            public void onBeforeClose(CefBrowser var1) {}

            @Override
            public boolean onBeforePopup(CefBrowser var1, CefFrame var2, String var3, String var4) {
                return false;
            }
        });

        return this.cefPanel;
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
    public JavascriptBridge getJavascriptBridge() {
        return this.bridge;
    }

    @Override
    public void createBrowser(@Nullable String url) {
        if (this.browser == null) {
            if (url == null) {
                url = "about:blank";
            }

            // Create browser
            this.browser = this.client.createBrowser(url, this.isOffScreenRenderingEnabled(), this.isTransparencyEnabled());

            // Add it to the JPanel.
            this.cefPanel.add(this.browser.getUIComponent(), BorderLayout.CENTER);

            // Notify
            this.getLifeCycleListener().onBrowserOpen();
        }
    }

    @Override
    public void destroyBrowser() {
        if (this.browser != null) {
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

}
