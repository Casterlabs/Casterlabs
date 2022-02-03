package co.casterlabs.caffeinated.app;

import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.webview.Webview;
import co.casterlabs.caffeinated.webview.WebviewFactory;
import co.casterlabs.caffeinated.webview.WebviewWindowState;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonString;

public class AppWindowState extends WebviewWindowState {

    @JsonSerializationMethod("icon")
    private JsonElement $serialize_icon() {
        Webview.getWebviewFactory();
        return new JsonString(WebviewFactory.getCurrentIcon());
    }

    @Override
    public void update() {
        super.update();

        new AsyncTask(() -> {
            CaffeinatedApp.getInstance().getWindowPreferences().save();
        });
    }

}
