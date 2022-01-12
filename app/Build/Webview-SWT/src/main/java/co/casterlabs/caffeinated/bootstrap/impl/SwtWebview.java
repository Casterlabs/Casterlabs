package co.casterlabs.caffeinated.bootstrap.impl;

import java.awt.Component;
import java.io.InputStream;
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

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.ui.UIPreferences;
import co.casterlabs.caffeinated.app.window.WindowPreferences;
import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.bootstrap.MainThread;
import co.casterlabs.caffeinated.bootstrap.tray.TrayHandler;
import co.casterlabs.caffeinated.bootstrap.ui.ApplicationUI;
import co.casterlabs.caffeinated.bootstrap.ui.ApplicationWindow;
import co.casterlabs.caffeinated.bootstrap.webview.AppWebview;
import co.casterlabs.caffeinated.bootstrap.webview.JavascriptBridge;
import co.casterlabs.caffeinated.util.Producer;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.util.async.Promise;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class SwtWebview extends AppWebview {
    private static Display display;

    @SuppressWarnings("deprecation")
    public static final Producer<AppWebview> FACTORY = () -> {
        Promise<Void> promise = new Promise<>();

        MainThread.submitTask(() -> {
            if (display == null) {
                display = Display.getDefault();
            }

            promise.fulfill(null);

            // We implement our own event loop for the MainThread.
            // This is required for SWT and AWT to play nicely with eachother.
            while (true) {

                // SWT GOT KILLED, THE END IS NEIGH.
                if (display.isDisposed()) {
                    Bootstrap.shutdown();
                    break;
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

        return new SwtWebview();
    };

    static {
        // Required for Linux: https://bugs.eclipse.org/bugs/show_bug.cgi?id=161911"
        System.setProperty("sun.awt.xembedserver", "true");
    }

    private SwtBridge bridge = new SwtBridge(this);
    private boolean hasPreloaded = false;

    private Browser browser;
    private Shell shell;

    @SneakyThrows
    @Override
    protected Component initialize0() {
        MainThread.submitTaskAndWait(this::mt_initialize);

        changeImage(CaffeinatedApp.getInstance().getUiPreferences().get().getIcon());
        CaffeinatedApp.getInstance().getUiPreferences().addSaveListener((PreferenceFile<UIPreferences> uiPreferences) -> {
            changeImage(uiPreferences.get().getIcon());
        });

        return null; // We handle the window ourselves.
    }

    private void mt_initialize() {
        // We create the shell and leave it open.
        // When the app wants it gone all we do is hide it.
        this.shell = new Shell(display, SWT.SHELL_TRIM);

        this.shell.setLayout(new FillLayout());

        this.browser = new Browser(this.shell, SWT.NONE);

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
                if (!event.title.equals("null")) {
                    shell.setText(event.title);
                }
            }
        });

        PreferenceFile<WindowPreferences> prefsFile = CaffeinatedApp.getInstance().getWindowPreferences();
        WindowPreferences prefs = prefsFile.get();

        this.shell.setMinimumSize(ApplicationWindow.MIN_WIDTH, ApplicationWindow.MIN_HEIGHT);
        this.shell.setBounds(prefs.getX(), prefs.getY(), prefs.getWidth(), prefs.getHeight());

        this.shell.addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent e) {
                Point loc = shell.getLocation();

                prefs.setX(loc.x);
                prefs.setY(loc.y);
                prefsFile.save();
            }

            @Override
            public void controlResized(ControlEvent e) {
                Point size = shell.getSize();

                prefs.setWidth(size.x);
                prefs.setHeight(size.y);
                prefsFile.save();
            }
        });

        this.shell.addListener(SWT.Close, (event) -> {
            event.doit = false;
            destroyBrowser();
        });

        display.getMenuBar().addListener(SWT.Activate, (event) -> {
            ApplicationUI.showWindow();
            event.doit = true;
        });

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
            String url = _url; // Pointer copy.

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
    public JavascriptBridge getJavascriptBridge() {
        return this.bridge;
    }

    @Override
    public void createBrowser(@Nullable String url) {
        if (!this.hasPreloaded) {
            this.hasPreloaded = true;
            // The following code initializes stuff related to AWT, which can't be done on
            // the main thread (it'll lock up). So we delegate it to another thread.
            MainThread.executeOffOfMainThread(() -> {
                this.getLifeCycleListener().onBrowserPreLoad();
            });
        }

        FastLogger.logStatic("create");
        TrayHandler.updateShowCheckbox(true);

        display.syncExec(() -> {
//            this.shell.pack();
            this.shell.open();

            this.loadURL(url);
        });
    }

    @Override
    public void destroyBrowser() {
        display.syncExec(() -> {
            // IMPL NOTE:
            // The shell is freed from the os if the JVM is shutting down cleanly.
            this.shell.setVisible(false);
            this.browser.setUrl("about:blank");
        });
        TrayHandler.updateShowCheckbox(false);
    }

    @SneakyThrows
    private void changeImage(String logo) {
        try (InputStream in = FileUtil.loadResourceAsUrl(String.format("assets/logo/%s.png", logo)).openStream()) {
            Image image = new Image(display, in);

            display.syncExec(() -> {

                this.shell.setImage(image);
            });
        }
    }

}
