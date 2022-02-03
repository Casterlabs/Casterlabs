package co.casterlabs.caffeinated.app;

import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.webview.WebviewWindowState;

public class AppWindowState extends WebviewWindowState {

    @Override
    public void update() {
        super.update();

        new AsyncTask(() -> {
            CaffeinatedApp.getInstance().getWindowPreferences().save();
        });
    }

}
