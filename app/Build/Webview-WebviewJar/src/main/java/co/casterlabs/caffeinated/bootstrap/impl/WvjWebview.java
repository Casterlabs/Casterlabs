package co.casterlabs.caffeinated.bootstrap.impl;

import java.awt.Component;

import org.jetbrains.annotations.Nullable;

import ca.weblite.webview.WebViewCLIClient;
import ca.weblite.webview.WebViewClient.MessageEvent;
import ca.weblite.webview.WebViewClient.OnLoadWebEvent;
import co.casterlabs.caffeinated.bootstrap.webview.AppWebview;
import co.casterlabs.caffeinated.bootstrap.webview.JavascriptBridge;
import co.casterlabs.caffeinated.util.Producer;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.consoleutil.platform.JavaPlatform;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class WvjWebview extends AppWebview {
    public static final Producer<AppWebview> FACTORY = () -> {
        return new WvjWebview();
    };

    static {
        if (ConsoleUtil.getPlatform() == JavaPlatform.WINDOWS) {
            FastLogger.logStatic(
                LogLevel.WARNING,
                "WebviewJava uses edge under the hood on Windows, this may result in buggy, slow, or a downright unusable experience.\nYou have been warned."
            );

//            LoopbackExemption.checkLoopback();
        }
    }

    private @Getter WebViewCLIClient webview;

    private WvjJavascriptBridge bridge = new WvjJavascriptBridge(this);
    private String currentUrl;

    @Override
    protected Component initialize0() {
        // Make sure it gets killed properly.
        Runtime.getRuntime().addShutdownHook(new Thread(this::destroyBrowser));

        return null;
    }

    @Override
    public void loadURL(@Nullable String url) {
        if (url == null) {
            url = "about:blank";
        }

        this.webview.eval(String.format("location.href = `%s`;", url));
    }

    @Override
    public String getCurrentURL() {
        return this.currentUrl;
    }

    @Override
    public void executeJavaScript(@NonNull String script) {
        if (this.webview.isDispatchThread()) {
            this.webview.eval(script);
        } else {
            this.webview.dispatch(() -> {
                this.webview.eval(script);
            });
        }
    }

    @Override
    public JavascriptBridge getJavascriptBridge() {
        return this.bridge;
    }

    private void onLoad(OnLoadWebEvent e) {
        this.currentUrl = e.getURL();
        this.bridge.injectBridgeScript();
    }

    private void onMessage(MessageEvent e) {
        new AsyncTask(() -> {
            this.bridge.query(e.getMessage());
        });
    }

    @SneakyThrows
    @Override
    public void createBrowser(@Nullable String url) {
        this.webview = (WebViewCLIClient) new WebViewCLIClient.Builder()
            .size(800, 600)
            .title("Casterlabs Caffeinated")
            .resizable(true)
            .url("about:blank")
            .build();

        this.webview.addMessageListener(this::onMessage);
        this.webview.addLoadListener(this::onLoad);

        this.webview.ready().get();

        this.loadURL(url);
    }

    @Override
    public void destroyBrowser() {
        try {
            webview.close();
        } catch (Exception e) {
            FastLogger.logException(e);
        }
    }

}
