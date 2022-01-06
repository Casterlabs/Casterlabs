package co.casterlabs.caffeinated.bootstrap.webview;

import java.awt.Component;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.bootstrap.webview.scheme.SchemeHandler;
import co.casterlabs.caffeinated.util.Crypto;
import co.casterlabs.caffeinated.util.Producer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AppWebview {
    public static final String WEBVIEW_SCHEME = "app";
    public static final String STATE_PASSWORD = new String(Crypto.generateSecureRandomKey());

    private static @Getter @Setter Producer<AppWebview> webviewFactory;

    private boolean initialized = false;

    private static @Getter @Setter SchemeHandler schemeHandler;

    private @Getter WebviewLifeCycleListener lifeCycleListener;

    private @Getter @Setter(AccessLevel.PROTECTED) boolean offScreenRenderingEnabled;
    private @Getter @Setter(AccessLevel.PROTECTED) boolean transparencyEnabled;

    public final Component initialize(boolean enableOSR, boolean enableTransparency) {
        assert !this.initialized : "Webview is already initialized.";

        this.offScreenRenderingEnabled = enableOSR;
        this.transparencyEnabled = enableTransparency;
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
