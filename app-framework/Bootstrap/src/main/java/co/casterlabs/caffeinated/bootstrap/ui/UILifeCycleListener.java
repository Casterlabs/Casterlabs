package co.casterlabs.caffeinated.bootstrap.ui;

public interface UILifeCycleListener {

    public void onPreLoad();

    public void onInitialLoad();

    /**
     * @return true to close the window, false to cancel.
     */
    public boolean onUICloseAttempt();

    public void onWindowOpen();

    public void onTrayMinimize();

}
