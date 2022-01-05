package co.casterlabs.caffeinated.bootstrap.webview.impl;

import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.bootstrap.webview.AppWebview;
import co.casterlabs.caffeinated.bootstrap.webview.JavascriptBridge;
import co.casterlabs.caffeinated.util.Producer;
import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.NonNull;
import netscape.javascript.JSObject;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class JfxWebview extends AppWebview {
    static String bridgeScript = "";

    public static final Producer<AppWebview> FACTORY = () -> {
        return new JfxWebview();
    };

    private JFXPanel panel;
    WebEngine engine;

    private JFXBridge bridge = new JFXBridge(this);

    static {
        // Get the javascript bridge.
        try {
            bridgeScript = FileUtil.loadResourceFromBuildProject("JFX_JavascriptBridge.js", "Webview-JavaFX");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO Clean this up, it's an ugly block of code.

        // TODO find a way to implement SchemeHandler, but the current implementation
        // should work, seeing as it's the same logic in the
        // ApplicationUI.AppSchemeHandler.

        URL.setURLStreamHandlerFactory((protocol) -> {
            if (protocol.equals(AppWebview.WEBVIEW_SCHEME)) {
                return new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL url) throws IOException {
                        String uri = url.getPath();

                        // Append `index.html` to the end when required.
                        if (!uri.contains(".")) {
                            if (uri.endsWith("/")) {
                                uri += "index.html";
                            } else {
                                uri += "/index.html";
                            }
                        }

                        URL resourceUrl = FileUtil.loadResourceAsUrl(url.getPath());

                        if (resourceUrl == null) {
                            throw new IOException("Resource not found: " + url);
                        } else {
                            return resourceUrl.openConnection();
                        }
                    }
                };
            } else {
                return null;
            }
        });
    }

    @Override
    protected Component initialize0() {
        this.panel = new JFXPanel();

        // Creation of scene and future interactions with JFXPanel
        // should take place on the JavaFX Application Thread
        Platform.runLater(() -> {
            WebView webView = new WebView();

            this.engine = webView.getEngine();
            this.panel.setScene(new Scene(webView));

            this.engine.setOnAlert((e) -> {
                FastLogger.logStatic(LogLevel.INFO, "Alert: %s", e.getData());
            });

//            try {
//                Class<?> webEngineClazz = WebEngine.class;
//
//                Field debuggerField = webEngineClazz.getDeclaredField("debugger");
//                debuggerField.setAccessible(true);
//
//                Debugger debugger = (Debugger) debuggerField.get(webView.getEngine());
//
//                // Open chrome://devtools/bundled/inspector.html?ws=localhost:51742/
//                new DevToolsDebuggerServer(debugger, 51742, 0, null, null);
//            } catch (Exception e) {
//                FastLogger.logException(e);
//            }

            this.engine
                .getLoadWorker()
                .stateProperty()
                .addListener((ov, oldState, newState) -> {
                    if (newState == State.RUNNING) {
                        // Inject the bridge.
                        JSObject jsobj = (JSObject) this.engine.executeScript("window");

                        jsobj.setMember("bridgeShim", this.bridge);

                        this.bridge.injectBridgeScript();
                    }

                });
        });

        return this.panel;
    }

    @Override
    public void loadURL(@Nullable String url) {
        Platform.runLater(() -> {
            if (url == null) {
                // Load a blank page.
                this.engine.loadContent("", "text/plain");
            } else {
                this.engine.load(url);
            }
        });
    }

    @Override
    public String getCurrentURL() {
        return this.engine.getLocation();
    }

    @Override
    public void executeJavaScript(@NonNull String script) {
        Platform.runLater(() -> {
            this.engine.executeScript(script);
        });
    }

    @Override
    public JavascriptBridge getJavascriptBridge() {
        return this.bridge;
    }

    @Override
    public void createBrowser(@Nullable String url) {
        this.loadURL(url);
        this.getLifeCycleListener().onBrowserOpen();
    }

    @Override
    public void destroyBrowser() {
        this.loadURL(null);
        this.getLifeCycleListener().onBrowserClose();
    }

}
