package co.casterlabs.caffeinated.bootstrap.ui;

import java.awt.BorderLayout;
import java.io.IOException;

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
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.bootstrap.cef.CefUtil;
import co.casterlabs.caffeinated.bootstrap.cef.bridge.JavascriptBridge;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.SchemeHandler;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.HttpRequest;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.HttpResponse;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.MimeTypes;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.StandardHttpStatus;
import co.casterlabs.caffeinated.bootstrap.tray.TrayHandler;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class ApplicationUI {
    private static UILifeCycleListener lifeCycleListener = null;

    // This is the real meat and potatoes.
    // This is auto injected into the GloballyAcessible AppBridge helper.
    private static @Getter JavascriptBridge bridge;

    private static @Getter ApplicationWindow window;
    private static @Getter ApplicationDevTools devtools;

    private static @Getter String appAddress;
    private static CefBrowser browser;
    private static CefClient client;

    private static @Getter boolean open = false;

    private static FastLogger logger = new FastLogger();

    public static void initialize(@NonNull String addr, @NonNull UILifeCycleListener listener) {
        appAddress = addr;
        lifeCycleListener = listener;

        client = CefUtil.createCefClient();

        window = new ApplicationWindow(listener);
        bridge = new JavascriptBridge(client);

        client.addContextMenuHandler(new CefContextMenuHandler() {
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

        logger.info("appAddress = %s", appAddress);

        logger.debug("Loadstate 0");
        lifeCycleListener.onPreLoad();
        client.addLoadHandler(new CefLoadHandlerAdapter() {
            // 0 = about:blank (preload)
            // 1 = app://index (load)
            // 2 = ... (completely loaded)
            private int loadState = 0;

            @Override
            public void onLoadEnd(CefBrowser _browser, CefFrame _frame, int httpStatusCode) {
                if (this.loadState == 0) {
                    logger.debug("Loadstate 1");
                    lifeCycleListener.onInitialLoad();
                    this.loadState = 2;
                    logger.debug("Loadstate 2");
                }
            }

            @Override
            public void onLoadStart(CefBrowser _browser, CefFrame _frame, TransitionType transitionType) {
                if (browser == _browser) {
                    logger.info("Injected Bridge.");
                    bridge.injectBridgeScript(browser.getMainFrame());

                    // Get that populated. (It's async)
                    window.updateBridgeData();
                }
            }

        });

        client.addLifeSpanHandler(new CefLifeSpanHandler() {

            @Override
            public boolean doClose(CefBrowser var1) {
                return false;
            }

            @Override
            public void onAfterCreated(CefBrowser _browser) {
                if (browser == _browser) {
                    logger.info("Created window.");
                    devtools = new ApplicationDevTools(browser);
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

        setTitle(null);
        showWindow();
    }

    public static void showWindow() {
        if (browser == null) {
            final boolean useOsr = CefUtil.enableOSR;
            final boolean isTransparent = CefUtil.enableTransparency;

            // Create browser
            browser = client.createBrowser(appAddress, useOsr, isTransparent);
            window.getCefPanel().add(browser.getUIComponent(), BorderLayout.CENTER);

            window.getFrame().setVisible(true);

            // Update state
            open = true;
            TrayHandler.updateShowCheckbox(true);

            // Notify
            lifeCycleListener.onWindowOpen();
        }
    }

    public static void closeWindow() {
        if (browser != null) {
            // Remove the frame
            window.getFrame().setVisible(false);
            window.getCefPanel().removeAll();

            // Close the browser
            if (ApplicationUI.getDevtools() != null) {
                ApplicationUI.getDevtools().close();
            }

            browser.close(false);
            browser = null;

            // Update state.
            open = false;
            TrayHandler.updateShowCheckbox(false);

            // Notify
            lifeCycleListener.onTrayMinimize();
        }
    }

    public static void setTitle(@Nullable String title) {
        if (title == null) {
            title = "Casterlabs Caffeinated";
        } else {
            title = "Casterlabs Caffeinated - " + title;
        }

        window.setTitle(title);
    }

    public static class AppSchemeHandler implements SchemeHandler {

        @Override
        public HttpResponse onRequest(HttpRequest request) {
            String uri = request.getUri().substring(Bootstrap.appUrl.length());

            // Append `index.html` to the end when required.
            if (!uri.contains(".")) {
                if (uri.endsWith("/")) {
                    uri += "index.html";
                } else {
                    uri += "/index.html";
                }
            }

            try {
                byte[] content = FileUtil.loadResourceBytes("app" + uri);
                String mimeType = "application/octet-stream";

                String[] split = uri.split("\\.");
                if (split.length > 1) {
                    mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
                }

                FastLogger.logStatic(LogLevel.DEBUG, "200 %s -> app%s (%s)", request.getUri(), uri, mimeType);

                return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                    .setMimeType(mimeType);
            } catch (IOException e) {
                FastLogger.logStatic(LogLevel.SEVERE, "404 %s -> app%s", request.getUri(), uri);

                return HttpResponse.newFixedLengthResponse(StandardHttpStatus.NOT_FOUND, "")
                    .setMimeType("application/octet-stream");
            }
        }

    }

    public static void focusAndBeep() {
        if (!open) {
            showWindow();
        }

        window.toFront();
        ConsoleUtil.bell();
    }

}
