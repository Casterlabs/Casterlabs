package co.casterlabs.caffeinated.bootstrap.impl;

import java.awt.Component;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import ca.weblite.webview.WebView;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
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

    private static String bridgeScript = "";

    static {
        if (ConsoleUtil.getPlatform() == JavaPlatform.WINDOWS) {
            FastLogger.logStatic(
                LogLevel.WARNING,
                "WebviewJava uses edge under the hood on Windows, this may result in buggy, slow, or a downright unusable experience.\nYou have been warned."
            );

//            LoopbackExemption.checkLoopback();
        }

        try {
            bridgeScript = FileUtil.loadResourceFromBuildProject("WVJ_JavascriptBridge.js", "Webview-WebviewJar");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private @Getter WebView webview;
    private AsyncTask webviewLoop;

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
        this.webview.dispatch(() -> {
            this.webview.eval(script);
        });
    }

    @Override
    public JavascriptBridge getJavascriptBridge() {
        return this.bridge;
    }

    private void onMessage(String message) {
        new AsyncTask(() -> {
            this.bridge.query(message);
        });
    }

    @SneakyThrows
    @Override
    public void createBrowser(@Nullable String url) {
        this.webview = new WebView()
            .size(800, 600)
            .title("Casterlabs Caffeinated")
            .resizable(true)
            .url("about:blank")
            .addOnBeforeLoad("ready();")
            .addJavascriptCallback("query", this::onMessage)
            .addJavascriptCallback("ready", (ignored) -> {
                this.executeJavaScript("delete window.ready;");
                this.executeJavaScript("document.body.innerText = 'test';");
                this.executeJavaScript(bridgeScript);
            });

        this.webviewLoop = new AsyncTask(this.webview::show);

        this.bridge.getLoadPromise().await();
//        this.loadURL(url);
    }

    @Override
    public void destroyBrowser() {
        try {
            this.webviewLoop.cancel();
        } catch (Exception e) {
            FastLogger.logException(e);
        }
    }

}
