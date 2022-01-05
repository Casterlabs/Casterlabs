package co.casterlabs.caffeinated.bootstrap.webview;

import co.casterlabs.caffeinated.app.bridge.AppBridge;
import co.casterlabs.caffeinated.util.DualConsumer;
import co.casterlabs.rakurai.json.element.JsonObject;

// This is the real meat and potatoes.
// This is auto injected into the GloballyAcessible AppBridge helper.
public abstract class JavascriptBridge extends AppBridge {

    public abstract void setOnEvent(DualConsumer<String, JsonObject> onEvent);

}
