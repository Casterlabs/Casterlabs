package co.casterlabs.caffeinated.webview;

public interface WebviewLifeCycleListener {

    public void onBrowserPreLoad();

    public void onBrowserOpen();

    public void onBrowserClose();

    public void onMinimize();

    public void onOpenRequested();

    public void onCloseRequested();

}
