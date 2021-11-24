package co.casterlabs.caffeinated.bootstrap.ui;

import java.awt.BorderLayout;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCommandLine;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefAppHandler;
import org.cef.handler.CefContextMenuHandler;
import org.cef.handler.CefLifeSpanHandler;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefPrintHandler;
import org.cef.network.CefRequest.TransitionType;
import org.jetbrains.annotations.Nullable;
import org.panda_lang.pandomium.Pandomium;
import org.panda_lang.pandomium.wrapper.PandomiumClient;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.bootstrap.cef.CefUtil;
import co.casterlabs.caffeinated.bootstrap.cef.bridge.JavascriptBridge;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.SchemeHandler;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.HttpRequest;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.HttpResponse;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.StandardHttpStatus;
import co.casterlabs.caffeinated.bootstrap.tray.TrayHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class ApplicationUI {
    private static UILifeCycleListener lifeCycleListener = null;

    private static @Getter JavascriptBridge bridge;
    private static @Getter ApplicationWindow window;
    private static @Getter ApplicationDevTools devtools;

    private static @Getter String appAddress;
    private static CefBrowser browser;
    private static PandomiumClient pandaClient;

    private static @Getter boolean open = false;

    private static FastLogger logger = new FastLogger();

    public static void initialize(@NonNull String addr, @NonNull UILifeCycleListener listener) {
        appAddress = addr;
        lifeCycleListener = listener;

        registerSchemes();

        Pandomium panda = CefUtil.createCefApp();
        pandaClient = panda.createClient();

        window = new ApplicationWindow(listener);
        bridge = new JavascriptBridge(pandaClient.getCefClient());

        pandaClient.getCefClient().addContextMenuHandler(new CefContextMenuHandler() {
            // ID | Name
            // ---+-------------------------
            // 01 | Inspect Element
            // 02 | Reload
            //

            @Override
            public void onBeforeContextMenu(CefBrowser browser, CefFrame frame, CefContextMenuParams params, CefMenuModel model) {
                model.clear();

                if (Bootstrap.isDev()) {
                    model.addItem(2, "Reload");

                    model.addCheckItem(1, "Inspect Element");
                    model.setChecked(1, devtools.isOpen());
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
        pandaClient.getCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
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
                }
            }

        });

        pandaClient.getCefClient().addLifeSpanHandler(new CefLifeSpanHandler() {

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
            // Create browser
            browser = pandaClient.loadURL(appAddress);
            window.getCefPanel().add(browser.getUIComponent(), BorderLayout.CENTER);

            // CEF needs to be visible in order to load the page.
            window.getFrame().setVisible(true);
            window.toFront();

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
            ApplicationUI.getDevtools().close();
            browser.close(false);
            browser = null;

            // Update state.
            open = false;
            TrayHandler.updateShowCheckbox(false);

            // Notify
            lifeCycleListener.onTrayMinimize();
        }
    }

    public static void registerSchemes() {
        CefApp.addAppHandler(new CefAppHandler() {

            @Override
            public void stateHasChanged(CefAppState var1) {}

            @Override
            public void onScheduleMessagePumpWork(long var1) {}

            @Override
            public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
                if (!registrar.addCustomScheme(
                    "app", // Scheme
                    true, // isStandard
                    false, // isLocal
                    false, // isDisplayIsolated
                    true, // isSecure
                    false, // isCorsEnabled
                    false, // isCspBypassing
                    true // isFetchEnabled
                )) {
                    FastLogger.logStatic(LogLevel.SEVERE, "Could not register scheme.");
                    System.exit(1);
                }
            }

            @Override
            public void onContextInitialized() {
                CefUtil.registerUrlScheme("app", new TestSchemeHandler());
            }

            @Override
            public boolean onBeforeTerminate() {
                return false;
            }

            @Override
            public void onBeforeCommandLineProcessing(String var1, CefCommandLine var2) {}

            @Override
            public CefPrintHandler getPrintHandler() {
                return null;
            }

        });
    }

    public static void setTitle(@Nullable String title) {
        if (title == null) {
            title = "Casterlabs Caffeinated";
        } else {
            title = "Casterlabs Caffeinated - " + title;
        }

        window.getFrame().setTitle(title);
    }

    // TODO read internal app files.
    public static class TestSchemeHandler implements SchemeHandler {

        @SneakyThrows
        @Override
        public HttpResponse onRequest(HttpRequest request) {
            String content = FileUtil.loadResource("test.html");

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content).setMimeType("text/html");
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
