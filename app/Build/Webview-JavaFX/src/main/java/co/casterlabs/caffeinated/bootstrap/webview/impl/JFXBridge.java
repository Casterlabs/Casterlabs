package co.casterlabs.caffeinated.bootstrap.webview.impl;

import co.casterlabs.caffeinated.app.bridge.BridgeValue;
import co.casterlabs.caffeinated.bootstrap.webview.JavascriptBridge;
import co.casterlabs.caffeinated.util.DualConsumer;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.util.async.Promise;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import javafx.application.Platform;
import lombok.NonNull;
import lombok.Setter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class JFXBridge extends JavascriptBridge {
    private Promise<Void> loadPromise = new Promise<>();

    private JfxWebview jfxWebview;

    public JFXBridge(JfxWebview jfxWebview) {
        this.jfxWebview = jfxWebview;
    }

    private @Setter DualConsumer<String, JsonObject> onEvent;
    private boolean hasPreloaded = false;

    public void injectBridgeScript() {
        Platform.runLater(() -> {
            this.jfxWebview.engine.executeScript(JfxWebview.bridgeScript);

            // Lifecycle listener. (Outside of the JavaFX thread)
            new AsyncTask(() -> {
                this.loadPromise.fulfill(null);

                // Both of these events should get fired right here.
                if (!this.hasPreloaded) {
                    this.hasPreloaded = true;
                    this.jfxWebview.getLifeCycleListener().onBrowserPreLoad();
                }

                this.jfxWebview.getLifeCycleListener().onBrowserInitialLoad();
            });
        });
    }

    @Override
    protected void emit0(@NonNull String type, @NonNull JsonElement data) {
        String script = String.format("window.Bridge.broadcast(%s,%s);", new JsonString(type), data);

        this.eval0(script);
    }

    @Override
    protected void eval0(@NonNull String script) {
        new AsyncTask(() -> {
            try {
                this.loadPromise.await();
            } catch (Throwable e) {}

            this.jfxWebview.executeJavaScript(script);
        });
    }

    private void handleEmission(@NonNull JsonObject query) {
        JsonObject emission = query.getObject("data");
        String type = emission.getString("type");
        JsonObject data = emission.getObject("data");

        FastLogger.logStatic(LogLevel.TRACE, "%s: %s", type, data);

        if (this.onEvent != null) {
            this.onEvent.accept(type, data);
        }
    }

    // Called by JavascriptBridge
    public void query(String request) {
        FastLogger.logStatic(LogLevel.TRACE, request);
        try {
            JsonObject query = Rson.DEFAULT.fromJson(request, JsonObject.class);

            switch (query.getString("type")) {
                case "emission": {
                    handleEmission(query);
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
