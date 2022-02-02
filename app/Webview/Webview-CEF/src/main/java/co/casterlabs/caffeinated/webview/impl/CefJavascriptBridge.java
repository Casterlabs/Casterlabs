package co.casterlabs.caffeinated.webview.impl;

import java.io.IOException;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import co.casterlabs.caffeinated.util.DualConsumer;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.util.async.Promise;
import co.casterlabs.caffeinated.webview.WebviewFileUtil;
import co.casterlabs.caffeinated.webview.bridge.WebviewBridge;
import co.casterlabs.caffeinated.webview.bridge.BridgeValue;
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

public class CefJavascriptBridge extends WebviewBridge {
    private static String bridgeScript = "";

    private CefMessageRouter router;
    private CefFrame frame;

    private Promise<Void> loadPromise = new Promise<>();

    private @Setter DualConsumer<String, JsonObject> onEvent;

    static {
        try {
            bridgeScript = WebviewFileUtil.loadResourceFromBuildProject("CEF_JavascriptBridge.js", "Webview-CEF");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CefJavascriptBridge(@NonNull CefClient client) {
        this.router = CefMessageRouter.create();

        this.router.addHandler(new CefMessageRouterHandlerAdapter() {

            @Override
            public boolean onQuery(
                CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent,
                CefQueryCallback callback
            ) {
                try {
                    JsonObject query = Rson.DEFAULT.fromJson(request, JsonObject.class);

                    switch (query.getString("type")) {
                        case "emission": {
                            if (!persistent) {
                                handleEmission(query);
                            }
                            break;
                        }

                        case "query": {
                            if (!persistent) {
                                String queryField = query.getString("field");
                                String queryNonce = query.getString("nonce");

                                BridgeValue<?> bv = CefJavascriptBridge.this.getQueryData().get(queryField);
                                JsonElement el = JsonNull.INSTANCE;

                                if (bv != null) {
                                    el = bv.getAsJson();
                                }

                                emit("querynonce:" + queryNonce, new JsonObject().put("data", el));
                            }
                            break;
                        }

                        default: {
                            callback.failure(-2, "Invalid payload type.");
                        }
                    }

                    callback.success("");
                } catch (JsonParseException ignored) {
                    callback.failure(-2, "Invalid JSON payload.");
                }

                return true;
            }

            @Override
            public void onQueryCanceled(CefBrowser browser, CefFrame frame, long queryId) {}

        }, true);

        client.addMessageRouter(this.router);
    }

    @Override
    public void emit(@NonNull String type, @NonNull JsonElement data) {
        new AsyncTask(() -> {
            try {
                this.loadPromise.await();
            } catch (Throwable e) {}

            String line = String.format("window.Bridge.broadcast(%s,%s);", new JsonString(type), data);

            this.frame.executeJavaScript(line, "", 1);
        });
    }

    @Override
    public void eval(@NonNull String script) {
        new AsyncTask(() -> {
            try {
                this.loadPromise.await();
            } catch (Throwable e) {}

            this.frame.executeJavaScript(script, "", 1);
        });
    }

    public void injectBridgeScript(@NonNull CefFrame frame) {
        // Inject the bridge script.
        this.frame = frame;
        this.frame.executeJavaScript(bridgeScript, "", 1);
        this.loadPromise.fulfill(null);
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

}
