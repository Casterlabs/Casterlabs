package co.casterlabs.caffeinated.ui;

public interface UILifeCycleListener {

    public void onPreLoad();

    public void onInitialLoad();

    /**
     * @return true to close the window, false to cancel.
     */
    public boolean onCloseAttempt();

    public void onTrayMinimize();

}
