package co.casterlabs.caffeinated.webview.impl;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.MainThread;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.util.async.Promise;
import co.casterlabs.caffeinated.webview.Webview;
import co.casterlabs.caffeinated.webview.WebviewFactory;
import co.casterlabs.caffeinated.webview.WebviewFileUtil;
import co.casterlabs.caffeinated.webview.bridge.WebviewBridge;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class WkWebview extends Webview {
    private static Display display;

    public static final WebviewFactory FACTORY = new WebviewFactory() {

        @SuppressWarnings("deprecation")
        @Override
        public @Nullable Webview produce() throws Exception {
            Promise<Void> promise = new Promise<>();

            MainThread.submitTask(() -> {
                if (display == null) {
                    Display.setAppName("Casterlabs Caffeinated");

                    display = Display.getDefault();
                }

                promise.fulfill(null);

                // We implement our own event loop for the MainThread.
                // This is required for SWT and AWT to play nicely with eachother.
                while (true) {

                    if (display.isDisposed()) {
                        // SWT GOT KILLED, THE END IS NEIGH.
                        Webview.getShutdown().run();
                        return;
                    } else {

                        // Let the main thread do some work (since we're blocking it right now)
                        MainThread.processTaskQueue();

                        // Execute the SWT dispatch and sleep if there is no more work to be done.
                        if (!display.readAndDispatch()) {
                            // We can't use display.sleep() or implement something similar in MainThread
                            // because they are separate systems without messaging, doing so would mean we
                            // would forfeit priority to one or the other.
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignored) {}
                        }
                    }
                }
            });

            try {
                promise.await();
            } catch (Throwable ignored) {}

            return new WkWebview();
        }

        @Override
        public boolean useNuclearOption() {
            return true;
        }

        @Override
        protected void setIcon0(@NonNull String icon) {
            for (WeakReference<Webview> wv : webviews) {
                ((WkWebview) wv.get()).changeImage(icon);
            }
        }
    };

    static {
        // Required for Linux: https://bugs.eclipse.org/bugs/show_bug.cgi?id=161911"
        System.setProperty("sun.awt.xembedserver", "true");
    }

    private WkBridge bridge = new WkBridge(this);
    private boolean hasPreloaded = false;

    private Browser browser;
    private Shell shell;

    @SneakyThrows
    @Override
    protected void initialize0() {
        MainThread.submitTaskAndWait(this::mt_initialize);

        this.changeImage(WebviewFactory.getCurrentIcon());
    }

    private void mt_initialize() {
        this.shell = new Shell(display, SWT.SHELL_TRIM);
        this.shell.setLayout(new FillLayout());

        this.browser = new Browser(this.shell, SWT.WEBKIT);

        this.browser.setUrl("about:blank");

//        try {
//            Object webkit = ReflectionLib.getValue(browser, "webBrowser"); // org.eclipse.swt.browser.WebKit
//            WebView view = ReflectionLib.getValue(webkit, "webView");
//
//            view.setApplicationNameForUserAgent(NSString.stringWith(String.format("WebKit; Just A CasterlabsCaffeinated (%s)", Webview.STATE_PASSWORD)));
//        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
//            e.printStackTrace();
//        }

        this.browser.addProgressListener(new ProgressListener() {
            @Override
            public void changed(ProgressEvent event) {
                bridge.injectBridgeScript();
            }

            @Override
            public void completed(ProgressEvent event) {}
        });

        this.browser.addTitleListener(new TitleListener() {
            @Override
            public void changed(TitleEvent event) {
                String title = event.title;

                if ((title == null) ||
                    title.equals("null") ||
                    title.equals("undefined") ||
                    title.isEmpty() ||
                    getCurrentURL().contains(title)) {
                    shell.setText("Casterlabs Caffeinated");
                } else {
                    shell.setText("Casterlabs Caffeinated - " + title);
                }
            }
        });

        this.shell.setMinimumSize(this.windowState.getMinHeight(), this.windowState.getMinHeight());
        this.shell.setBounds(this.windowState.getX(), this.windowState.getY(), this.windowState.getWidth(), this.windowState.getHeight());

        this.shell.addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent e) {
                Point loc = shell.getLocation();

                windowState.setX(loc.x);
                windowState.setY(loc.y);
                windowState.update();
            }

            @Override
            public void controlResized(ControlEvent e) {
                Point size = shell.getSize();

                windowState.setWidth(size.x);
                windowState.setHeight(size.y);
                windowState.update();
            }
        });

        this.shell.addListener(SWT.Close, (event) -> {
            event.doit = false;
            this.getLifeCycleListener().onCloseRequested();
        });

        if (display.getMenuBar() != null) {
            display.getMenuBar().addListener(SWT.Activate, (event) -> {
                event.doit = true;
                this.getLifeCycleListener().onOpenRequested();
            });
        }

        new AsyncTask(() -> {
            // The bridge query code.
            // Note that AsyncTask will not hold the JVM open, so we can safely use it
            // without a shutdown mechanism.
            while (!this.shell.isDisposed()) {
                String result = (String) this.eval("return window.Bridge?.clearQueryQueue();");

                if (result != null) {
                    try {
                        JsonArray arr = Rson.DEFAULT.fromJson(result, JsonArray.class);

                        for (JsonElement e : arr) {
                            bridge.query(e.getAsString());
                        }
                    } catch (JsonParseException e) {
                        FastLogger.logException(e);
                    }
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (InterruptedException e) {}
            }
        });
    }

    @Override
    public void loadURL(@Nullable String _url) {
        display.asyncExec(() -> {
            String url = _url; // Pointer.

            if (url == null) {
                url = "about:blank";
            }

            this.browser.setUrl(url);
        });
    }

    @Override
    public String getCurrentURL() {
        return new SwtPromise<String>(() -> this.browser.getUrl()).await();
    }

    @Override
    public void executeJavaScript(@NonNull String script) {
        display.asyncExec(() -> {
            this.browser.execute(script);
        });
    }

    private Object eval(String line) {
        return new SwtPromise<Object>(() -> this.browser.evaluate(line, true)).await();
    }

    @Override
    public WebviewBridge getBridge() {
        return this.bridge;
    }

    @Override
    public void open(@Nullable String url) {
        if (!this.hasPreloaded) {
            this.hasPreloaded = true;
            // The following code initializes stuff related to AWT, which can't be done on
            // the main thread (it'll lock up). So we delegate it to another thread.
            MainThread.executeOffOfMainThread(() -> {
                this.getLifeCycleListener().onBrowserPreLoad();
            });
        }

        FastLogger.logStatic("create");
        this.getLifeCycleListener().onBrowserOpen();

        display.syncExec(() -> {
            this.mt_initialize();
//            this.shell.pack();
            this.shell.open();
            this.shell.setActive();

            this.loadURL(url);
        });
    }

    @Override
    public void close() {
        display.syncExec(() -> {
            // We destroy the shell to prevent it from sticking in the Dock.
            this.shell.setVisible(false);
            this.browser.setUrl("about:blank");
        });

        this.getLifeCycleListener().onBrowserClose();
    }

    @Override
    public void destroy() {
        display.syncExec(() -> {
            this.shell.dispose();
        });
    }

    @SneakyThrows
    private void changeImage(String logo) {
        if (logo != null) {
            try (InputStream in = WebviewFileUtil.loadResourceAsUrl(String.format("assets/logo/%s.png", logo)).openStream()) {
                Image image = new Image(display, in);

                display.syncExec(() -> {
                    this.shell.setImage(image);
                });
            }
        }
    }

    @Override
    public void focus() {
        display.syncExec(() -> {
            this.shell.setActive();
        });
    }

    @Override
    public boolean isOpen() {
        return new SwtPromise<>(() -> {
            return this.shell.isVisible();
        }).await();
    }

}
