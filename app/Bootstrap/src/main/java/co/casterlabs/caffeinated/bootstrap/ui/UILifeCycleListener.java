package co.casterlabs.caffeinated.bootstrap.ui;

import co.casterlabs.caffeinated.bootstrap.webview.WebviewLifeCycleListener;

public interface UILifeCycleListener extends WebviewLifeCycleListener {

    /**
     * @return true to close the window, false to cancel.
     */
    public boolean onUICloseAttempt();

    public void onMinimize();

}
