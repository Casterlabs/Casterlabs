package co.casterlabs.caffeinated.cef.bridge;

import java.io.IOException;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import co.casterlabs.caffeinated.FileUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;

@SuppressWarnings("unused")
public class JavascriptBridge {
    private static String bridgeScript = "";

    private CefClient client;
    private CefMessageRouter router;

    private CefQueryCallback sub;
    private long subId;

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
            public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
                try {
                    JsonObject query = Rson.DEFAULT.fromJson(request, JsonObject.class);

                    switch (query.getString("type")) {
                        case "emission": {
                            if (!persistent) {
                                handleEmission(query);
                                callback.success("");
                            }
                            break;
                        }

                        case "subscribe": {
                            if (persistent && (sub == null)) {
                                subId = queryId;
                                sub = callback;
                            }
                            break;
                        }

                        default: {
                            callback.failure(-2, "Invalid payload type.");
                        }
                    }
                } catch (JsonParseException ignored) {
                    callback.failure(-2, "Invalid JSON payload.");
                }

                return true;
            }

            @Override
            public void onQueryCanceled(CefBrowser browser, CefFrame frame, long queryId) {
                if (subId == queryId) {
                    subId = -1;
                    sub = null;
                }
            }

        }, true);

        client.addMessageRouter(this.router);
    }

    public void injectBridgeScript(CefFrame frame) {
        // Inject the bridge script.
        frame.executeJavaScript(bridgeScript, "", 1);
    }

    public void emit(String type, JsonObject data) {
        if (this.sub != null) {
            JsonObject payload = new JsonObject()
                .put("type", type)
                .put("data", data);

            this.sub.success(payload.toString());
        }
    }

    private void handleEmission(JsonObject query) {
        JsonObject emission = query.getObject("data");

        // TODO integrate event handling (JavascriptBridge#on method)
        System.out.println(emission);
    }

}
