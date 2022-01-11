package co.casterlabs.caffeinated.bootstrap.impl;

import java.awt.Component;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.window.WindowPreferences;
import co.casterlabs.caffeinated.bootstrap.tray.TrayHandler;
import co.casterlabs.caffeinated.bootstrap.ui.ApplicationWindow;
import co.casterlabs.caffeinated.bootstrap.webview.AppWebview;
import co.casterlabs.caffeinated.bootstrap.webview.JavascriptBridge;
import co.casterlabs.caffeinated.util.Producer;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class SwtWebview extends AppWebview {
    private static Display display;

    public static final Producer<AppWebview> FACTORY = () -> {
        display = new Display(); // This gets called on the main thread.

        return new SwtWebview();
    };

    static {
        // Required for Linux: https://bugs.eclipse.org/bugs/show_bug.cgi?id=161911"
        System.setProperty("sun.awt.xembedserver", "true");
    }

    private SwtBridge bridge = new SwtBridge(this);

    private Browser browser;
    private Shell shell;

    @Override
    protected Component initialize0() {
        new AsyncTask(() -> {
            while (true) {
                if (browser != null) {
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
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {}
            }
        });

        return null; // We handle the window ourselves.
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
        display.syncExec(() -> {
            this.shell = new Shell(display, SWT.SHELL_TRIM);

            this.shell.setLayout(new FillLayout());

            this.browser = new Browser(this.shell, SWT.NONE);

            this.browser.addProgressListener(new ProgressListener() {
                @Override
                public void changed(ProgressEvent event) {}

                @Override
                public void completed(ProgressEvent event) {
                    bridge.injectBridgeScript();
                }
            });

            this.browser.addTitleListener(new TitleListener() {
                @Override
                public void changed(TitleEvent event) {
                    shell.setText(event.title);
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

            this.shell.pack();
            this.shell.open();

            this.loadURL(url);

            while ((shell != null) && !shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        });

        TrayHandler.updateShowCheckbox(true);
    }

    @Override
    public void destroyBrowser() {
        display.asyncExec(() -> {
            this.shell.dispose();
            this.shell = null;
            this.browser = null;

            TrayHandler.updateShowCheckbox(false);
        });
    }

}
