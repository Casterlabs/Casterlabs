package co.casterlabs.caffeinated.bootstrap.cef.bridge;

import java.io.IOException;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import lombok.Setter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class JavascriptBridge {
    private static String bridgeScript = "";

    private CefMessageRouter router;
    private CefFrame frame;

    private @Getter JsonObject queryData = new JsonObject();

    private @Setter DualConsumer<String, JsonObject> onEvent;

    static {
        try {
            bridgeScript = FileUtil.loadResource("JavascriptBridge.js");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JavascriptBridge(CefClient client) {
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

                                JsonElement data = queryData.get(queryField);

                                emit("querynonce:" + queryNonce, new JsonObject().put("data", data));
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

    public void injectBridgeScript(CefFrame frame) {
        // Inject the bridge script.
        this.frame = frame;
        this.frame.executeJavaScript(bridgeScript, "", 1);
    }

    public void emit(String type, JsonElement data) {
        String line = String.format("window.Bridge.broadcast(%s,%s);", new JsonString(type), data);

        this.frame.executeJavaScript(line, "", 1);
    }

    private void handleEmission(JsonObject query) {
        JsonObject emission = query.getObject("data");
        String type = emission.getString("type");
        JsonObject data = emission.getObject("data");

        FastLogger.logStatic(LogLevel.TRACE, "%s: %s", type, data);

        if (this.onEvent != null) {
            this.onEvent.accept(type, data);
        }
    }

}
