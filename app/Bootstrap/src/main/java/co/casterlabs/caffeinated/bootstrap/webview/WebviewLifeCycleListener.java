package co.casterlabs.caffeinated.bootstrap.webview;

public interface WebviewLifeCycleListener {

    public void onBrowserPreLoad();

    public void onBrowserInitialLoad();

    public void onBrowserOpen();

    public void onBrowserClose();

}
