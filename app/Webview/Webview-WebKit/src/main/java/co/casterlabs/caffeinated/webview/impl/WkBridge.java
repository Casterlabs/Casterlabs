package co.casterlabs.caffeinated.webview.impl;

import java.io.IOException;

import co.casterlabs.caffeinated.util.DualConsumer;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.webview.WebviewFileUtil;
import co.casterlabs.caffeinated.webview.bridge.BridgeValue;
import co.casterlabs.caffeinated.webview.bridge.WebviewBridge;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;
import lombok.Setter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class WkBridge extends WebviewBridge {
    private static String bridgeScript = "";

    private WkWebview webview;

    private @Setter DualConsumer<String, JsonObject> onEvent;

    static {
        // Get the javascript bridge.
        try {
            bridgeScript = WebviewFileUtil.loadResourceFromBuildProject("SWT_JavascriptBridge.js", "Webview-Webkit");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WkBridge(WkWebview webview) {
        this.webview = webview;
    }

    @Override
    public void emit(@NonNull String type, @NonNull JsonElement data) {
        String script = String.format("window.Bridge.broadcast(%s,%s);", new JsonString(type), data);

//        if (!type.startsWith("querynonce:")) {
        FastLogger.logStatic(LogLevel.TRACE, "emission [%s]: %s", type, data);
//        }

        this.eval(script);
    }

    @Override
    public void eval(@NonNull String script) {
        webview.executeJavaScript(script);
    }

    public void injectBridgeScript() {
        this.eval(bridgeScript);

        // Lifecycle listener. (Outside of the main thread)
        new AsyncTask(() -> {
//            this.loadPromise.fulfill(null);
            this.attachBridge(this.webview.getWindowState().getBridge());
        });
    }

    // Called by SwtWebview
    public void query(String request) {
        FastLogger.logStatic(LogLevel.TRACE, request);

        try {
            JsonObject query = Rson.DEFAULT.fromJson(request, JsonObject.class);

            switch (query.getString("type")) {
                case "emission": {
                    JsonObject emission = query.getObject("data");
                    String type = emission.getString("type");
                    JsonObject data = emission.getObject("data");

                    if (this.onEvent != null) {
                        this.onEvent.accept(type, data);
                    }
                    break;
                }

                case "query": {
                    String queryField = query.getString("field");
                    String queryNonce = query.getString("nonce");

                    BridgeValue<?> bv = this.getQueryData().get(queryField);
                    JsonElement el = JsonNull.INSTANCE;

                    if (bv != null) {
                        el = bv.getAsJson();
                    }

                    emit("querynonce:" + queryNonce, new JsonObject().put("data", el));
                    break;
                }
            }
        } catch (JsonParseException ignored) {}
    }

}
