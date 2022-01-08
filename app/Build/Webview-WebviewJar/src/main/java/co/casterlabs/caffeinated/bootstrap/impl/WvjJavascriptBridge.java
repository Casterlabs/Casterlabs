package co.casterlabs.caffeinated.bootstrap.impl;

import java.io.Closeable;
import java.io.IOException;

import co.casterlabs.caffeinated.app.bridge.BridgeValue;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.bootstrap.webview.JavascriptBridge;
import co.casterlabs.caffeinated.util.DualConsumer;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.util.async.Promise;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.NonNull;
import lombok.Setter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class WvjJavascriptBridge extends JavascriptBridge implements Closeable {
    private static String bridgeScript = "";

    private WvjWebview webview;

    private Promise<Void> loadPromise;
    private @Setter DualConsumer<String, JsonObject> onEvent;
    private boolean hasPreloaded = false;

    public WvjJavascriptBridge(WvjWebview webview) {
        this.webview = webview;
        this.close();
    }

    static {
        try {
            bridgeScript = FileUtil.loadResourceFromBuildProject("WVJ_JavascriptBridge.js", "Webview-WebviewJar");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Called by WvjWebview
    public void query(String request) {
        if (!request.isEmpty()) {
            FastLogger.logStatic(LogLevel.TRACE, request);
            try {
                JsonArray requestData = Rson.DEFAULT.fromJson(request, JsonArray.class);

                JsonElement requestElement = requestData.get(0);

                if (requestElement.isJsonObject()) {
                    JsonObject query = requestElement.getAsObject();

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
                }
            } catch (Exception e) {
                FastLogger.logException(e);
            }
        }
    }

    public void handleEmission(@NonNull JsonObject query) {
        JsonObject emission = query.getObject("data");
        String type = emission.getString("type");

        if (type.equals("internal::ready")) {
            FastLogger.logStatic(LogLevel.DEBUG, "READY!");

            // Lifecycle listener. (Outside of the dispatch thread)
            new AsyncTask(() -> {
                this.loadPromise.fulfill(null);

                // Both of these events should get fired right here.
                if (!this.hasPreloaded) {
                    this.hasPreloaded = true;
                    this.webview.getLifeCycleListener().onBrowserPreLoad();
                }

                this.webview.getLifeCycleListener().onBrowserInitialLoad();
            });
        } else {
            JsonObject data = emission.getObject("data");

            FastLogger.logStatic(LogLevel.TRACE, "%s: %s", type, data);

            if (this.onEvent != null) {
                this.onEvent.accept(type, data);
            }
        }
    }

    public void injectBridgeScript() {
        this.webview.executeJavaScript(bridgeScript);
    }

    @Override
    protected void emit0(@NonNull String type, @NonNull JsonElement data) {
        new AsyncTask(() -> {
            try {
                this.loadPromise.await();
            } catch (Throwable e) {}

            String script = String.format("window.Bridge.broadcast(%s,%s);", new JsonString(type), data);

            this.webview.executeJavaScript(script);
        });
    }

    @Override
    protected void eval0(@NonNull String script) {
        new AsyncTask(() -> {
            try {
                this.loadPromise.await();
            } catch (Throwable e) {}

            this.webview.executeJavaScript(script);
        });
    }

    @Override
    public void close() {
        this.loadPromise = new Promise<>();
    }

}
