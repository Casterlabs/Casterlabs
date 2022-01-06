package co.casterlabs.caffeinated.bootstrap.webview;

import java.awt.Component;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.bootstrap.webview.scheme.SchemeHandler;
import co.casterlabs.caffeinated.util.Crypto;
import co.casterlabs.caffeinated.util.Producer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AppWebview {
    private static @Getter @Setter boolean offScreenRenderingEnabled = System.getProperty("caffeinated.cef.offscreenrendering.enable", "").equals("true"); // Defaults to false;
    private static @Getter @Setter boolean transparencyEnabled = System.getProperty("caffeinated.cef.transparency.enable", "").equals("true"); // Defaults to false

    public static final String WEBVIEW_SCHEME = "app";
    public static final String STATE_PASSWORD = new String(Crypto.generateSecureRandomKey());

    private static @Getter @Setter Producer<AppWebview> webviewFactory;

    private boolean initialized = false;

    private static @Getter @Setter SchemeHandler schemeHandler;

    private @Getter WebviewLifeCycleListener lifeCycleListener;

    public final Component initialize() {
        assert !this.initialized : "Webview is already initialized.";

        return this.initialize0();
    }

    public final void setLifeCycleListener(@NonNull WebviewLifeCycleListener lifeCycleListener) {
        assert !this.initialized : "Webview is already initialized, setLifeCycleListener must be called BEFORE initialization.";

        this.lifeCycleListener = lifeCycleListener;
    }

    protected abstract Component initialize0();

    public abstract void loadURL(@Nullable String url);

    public abstract String getCurrentURL();

    public abstract void executeJavaScript(@NonNull String script);

    public abstract JavascriptBridge getJavascriptBridge();

    public abstract void createBrowser(@Nullable String url);

    public abstract void destroyBrowser();

}
