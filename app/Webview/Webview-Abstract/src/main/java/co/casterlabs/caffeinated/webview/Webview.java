package co.casterlabs.caffeinated.webview;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.util.Crypto;
import co.casterlabs.caffeinated.webview.bridge.WebviewBridge;
import co.casterlabs.caffeinated.webview.scheme.SchemeHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class Webview {
    private static @Getter @Setter boolean offScreenRenderingEnabled = System.getProperty("caffeinated.cef.offscreenrendering.enable", "").equals("true"); // Defaults to false;
    private static @Getter @Setter boolean transparencyEnabled = System.getProperty("caffeinated.cef.transparency.enable", "").equals("true"); // Defaults to false

    public static final String WEBVIEW_SCHEME = "app";
    public static final String STATE_PASSWORD = new String(Crypto.generateSecureRandomKey());
    public static final boolean isDev = false;

    private static @Getter @Setter WebviewFactory webviewFactory;
    private static @Getter @Setter SchemeHandler schemeHandler;

    private boolean initialized = false;

    private @Getter WebviewLifeCycleListener lifeCycleListener;
    protected @Getter WebviewWindowState windowState = new WebviewWindowState();

    public final void initialize(@Nullable WebviewWindowState windowState) throws Exception {
        assert !this.initialized : "Webview is already initialized.";

        if (windowState != null) {
            this.windowState = windowState;
        }

        this.initialized = true;
        this.initialize0();
    }

    public final void setLifeCycleListener(@NonNull WebviewLifeCycleListener lifeCycleListener) {
        assert !this.initialized : "Webview is already initialized, setLifeCycleListener must be called BEFORE initialization.";

        this.lifeCycleListener = lifeCycleListener;
    }

    protected abstract void initialize0() throws Exception;

    public abstract void loadURL(@Nullable String url);

    public abstract String getCurrentURL();

    public abstract void executeJavaScript(@NonNull String script);

    public abstract WebviewBridge getBridge();

    public abstract void open(@Nullable String url);

    public abstract void close();

    public abstract void destroy();

    public abstract void focus();

    public abstract boolean isOpen();

}
