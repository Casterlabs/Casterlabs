package co.casterlabs.caffeinated.bootstrap.impl;

import java.awt.Component;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jetbrains.annotations.Nullable;

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
        Display.getDefault().asyncExec(() -> {
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
        Display.getDefault().asyncExec(() -> {
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
        Display.getDefault().syncExec(() -> {
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

            this.browser.setBounds(0, 0, 600, 400);

            this.shell.pack();
            this.shell.open();

            this.loadURL(url);

            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        });
    }

    @Override
    public void destroyBrowser() {
        Display.getDefault().syncExec(() -> {
            this.shell.dispose();
            this.shell = null;
            this.browser = null;
        });
    }

}
