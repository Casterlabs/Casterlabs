package co.casterlabs.caffeinated.app.ui.popouts;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.webview.Webview;
import co.casterlabs.caffeinated.webview.WebviewLifeCycleListener;
import co.casterlabs.caffeinated.webview.WebviewWindowState;
import lombok.SneakyThrows;

public abstract class UIPopout implements WebviewLifeCycleListener {
    private PreferenceFile<DockWindowState> state;
    private Webview webview;

    @SuppressWarnings("deprecation")
    @SneakyThrows
    public UIPopout(PreferenceFile<DockWindowState> state) {
        this.state = state;
        this.webview = Webview.getWebviewFactory().produce();

        DockWindowState dws = this.state.get();
        dws.popout = this;

        this.webview.setSchemeHandler(CaffeinatedApp.getInstance().getWebview().getSchemeHandler());
        this.webview.initialize(this, dws, false, false);

        this.webview.getBridge().mergeWith(CaffeinatedApp.getInstance().getAppBridge());
    }

    protected abstract String getUrl();

    @Override
    public void onOpenRequested() {
        this.open();
    }

    @Override
    public void onCloseRequested() {
        this.webview.close();
    }

    public void open() {
        if (this.webview.isOpen()) {
            this.webview.focus();
        } else {
            this.webview.open(CaffeinatedApp.getInstance().getAppUrl() + this.getUrl());
        }
    }

    public void destroy() {
        this.webview.destroy();
    }

    public static class DockWindowState extends WebviewWindowState {
        private UIPopout popout;

        @Override
        public void update() {
            popout.state.save();
        }

    }

    /* Unused */
    @Override
    public void onBrowserPreLoad() {}

    @Override
    public void onBrowserOpen() {}

    @Override
    public void onBrowserClose() {}

    @Override
    public void onMinimize() {}

}
