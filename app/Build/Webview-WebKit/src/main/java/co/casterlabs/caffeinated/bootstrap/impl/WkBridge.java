package co.casterlabs.caffeinated.bootstrap.impl;

import java.io.IOException;

import co.casterlabs.caffeinated.app.bridge.BridgeValue;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.bootstrap.webview.JavascriptBridge;
import co.casterlabs.caffeinated.util.DualConsumer;
import co.casterlabs.caffeinated.util.async.AsyncTask;
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

public class WkBridge extends JavascriptBridge {
    private static String bridgeScript = "";

    private WkWebview webview;

    private @Setter DualConsumer<String, JsonObject> onEvent;

    static {
        // Get the javascript bridge.
        try {
            bridgeScript = FileUtil.loadResourceFromBuildProject("SWT_JavascriptBridge.js", "Webview-SWT");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WkBridge(WkWebview webview) {
        this.webview = webview;
    }

    @Override
    protected void emit0(@NonNull String type, @NonNull JsonElement data) {
        String script = String.format("window.Bridge.broadcast(%s,%s);", new JsonString(type), data);

//        if (!type.startsWith("querynonce:")) {
        FastLogger.logStatic(LogLevel.TRACE, "emission [%s]: %s", type, data);
//        }

        this.eval0(script);
    }

    @Override
    protected void eval0(@NonNull String script) {
        webview.executeJavaScript(script);
    }

    public void injectBridgeScript() {
        this.eval0(bridgeScript);

        // Lifecycle listener. (Outside of the JavaFX thread)
        new AsyncTask(() -> {
//            this.loadPromise.fulfill(null);
            this.webview.getLifeCycleListener().onBrowserInitialLoad();
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

                    BridgeValue<?> bv = queryData.get(queryField);
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
