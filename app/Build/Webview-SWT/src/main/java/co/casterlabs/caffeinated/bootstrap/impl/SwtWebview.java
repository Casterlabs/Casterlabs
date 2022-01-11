package co.casterlabs.caffeinated.bootstrap.impl;

import java.awt.Component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.bootstrap.webview.AppWebview;
import co.casterlabs.caffeinated.bootstrap.webview.JavascriptBridge;
import co.casterlabs.caffeinated.util.Producer;
import lombok.NonNull;

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

    private Shell shell;

    private Browser browser;

    @Override
    protected Component initialize0() {
        return null; // We handle the window ourselves.
    }

    @Override
    public void loadURL(@Nullable String url) {

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

    @Override
    public JavascriptBridge getJavascriptBridge() {
        return null;
    }

    @Override
    public void createBrowser(@Nullable String url) {
        Display.getDefault().syncExec(() -> {
            this.shell = new Shell(display, SWT.SHELL_TRIM);

            this.shell.setLayout(new FillLayout());

            this.browser = new Browser(this.shell, SWT.NONE);

            this.browser.addTitleListener(new TitleListener() {
                @Override
                public void changed(TitleEvent event) {
                    shell.setText(event.title);
                }
            });

            this.browser.setBounds(0, 0, 600, 400);

            this.shell.pack();
            this.shell.open();

            this.browser.setUrl(url);

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
