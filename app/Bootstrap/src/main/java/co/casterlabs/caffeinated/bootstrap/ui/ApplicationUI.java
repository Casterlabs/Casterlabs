package co.casterlabs.caffeinated.bootstrap.ui;

import java.awt.Component;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.bootstrap.tray.TrayHandler;
import co.casterlabs.caffeinated.bootstrap.webview.AppWebview;
import co.casterlabs.caffeinated.bootstrap.webview.scheme.SchemeHandler;
import co.casterlabs.caffeinated.bootstrap.webview.scheme.http.HttpRequest;
import co.casterlabs.caffeinated.bootstrap.webview.scheme.http.HttpResponse;
import co.casterlabs.caffeinated.bootstrap.webview.scheme.http.MimeTypes;
import co.casterlabs.caffeinated.bootstrap.webview.scheme.http.StandardHttpStatus;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class ApplicationUI {
    private static FastLogger logger = new FastLogger();

    private static @Getter String appAddress;
    private static AppWebview webview;
    private static @Getter boolean open = false;
    private static boolean customImplementation = false;

    private static @Getter ApplicationWindow window;

    public static void initialize(@NonNull String addr, @NonNull AppWebview webview, @NonNull UILifeCycleListener uiLifeCycleListener) {
        appAddress = addr;

        ApplicationUI.webview = webview;

        // Initialize the webview
        Component webviewComponent = webview.initialize();

        if (webviewComponent == null) {
            // The webview implements it's own Windowing system.
            customImplementation = true;
        } else {
            // Create the window
            window = new ApplicationWindow(uiLifeCycleListener, webviewComponent);
        }

        logger.info("appAddress = %s", appAddress);

        setTitle(null);
        showWindow();
    }

    public static void showWindow() {
        if (!open) {
            webview.createBrowser(appAddress);

            if (!customImplementation) {
                window.getFrame().setVisible(true);
            }

            // Update state
            open = true;
            TrayHandler.updateShowCheckbox(true);
        }
    }

    public static void closeWindow() {
        if (open) {
            if (!customImplementation) {
                // Hide the frame
                window.getFrame().setVisible(false);
            }

            // Destsroy the browser.
            webview.destroyBrowser();

            // Update state.
            open = false;
            TrayHandler.updateShowCheckbox(false);
        }
    }

    public static void setTitle(@Nullable String title) {
        webview.setTitle(title);

        if (!customImplementation) {
            if (title == null) {
                title = "Casterlabs Caffeinated";
            } else {
                title = "Casterlabs Caffeinated - " + title;
            }

            window.setTitle(title);
        }
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

        if (!customImplementation) {
            window.toFront();
        }

        ConsoleUtil.bell();
    }

    public static void dispose() {
        closeWindow();

        if (!customImplementation) {
            window.dispose();
        }
    }

}
