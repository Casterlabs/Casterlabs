package co.casterlabs.caffeinated.webview.bridge;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.casterlabs.caffeinated.util.DualConsumer;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.Setter;

public abstract class WebviewBridge {
    private static Map<String, BridgeValue<?>> globalQueryData = new HashMap<>();
    private static List<WeakReference<WebviewBridge>> bridges = new ArrayList<>();

    private WeakReference<WebviewBridge> $ref = new WeakReference<>(this);

    private Map<String, BridgeValue<?>> personalQueryData = new HashMap<>();

    private List<WeakReference<WebviewBridge>> downstreamBridges = new LinkedList<>();
    private List<WeakReference<WebviewBridge>> attachedBridges = new LinkedList<>();

    protected @Setter DualConsumer<String, JsonObject> onEvent;

    public WebviewBridge() {
        bridges.add(this.$ref);
    }

    @Deprecated
    public void mergeWith(WebviewBridge parent) {
        parent.downstreamBridges.add(this.$ref);
        this.attachedBridges.add(parent.$ref);
        this.personalQueryData = parent.personalQueryData; // Pointer copy.
        this.onEvent = parent.onEvent;
    }

    public void attachValue(@NonNull BridgeValue<?> bv) {
        this.personalQueryData.put(bv.getKey(), bv);
        bv.attachedBridges.add(this.$ref);
    }

    protected Map<String, BridgeValue<?>> getQueryData() {
        Map<String, BridgeValue<?>> combined = new HashMap<>();

        combined.putAll(this.personalQueryData);
        combined.putAll(globalQueryData);

        return combined;
    }

    @Override
    protected void finalize() {
        bridges.remove(this.$ref);

        for (BridgeValue<?> bv : this.personalQueryData.values()) {
            bv.attachedBridges.remove(this.$ref);
        }

        for (WeakReference<WebviewBridge> wb : this.attachedBridges) {
            wb.get().downstreamBridges.remove(this.$ref);
        }
    }

    public void emit(@NonNull String type, @NonNull JsonElement data) {
        this.emit0(type, data);

        for (WeakReference<WebviewBridge> wb : this.downstreamBridges) {
            wb.get().emit0(type, data);
        }
    }

    public void eval(@NonNull String script) {
        this.eval0(script);

        for (WeakReference<WebviewBridge> wb : this.downstreamBridges) {
            wb.get().eval0(script);
        }
    }

    /* Impl */

    protected abstract void emit0(@NonNull String type, @NonNull JsonElement data);

    protected abstract void eval0(@NonNull String script);

    /* Statics */

    protected static void attachGlobalBridge(@NonNull BridgeValue<?> bv) {
        globalQueryData.put(bv.getKey(), bv);
    }

    public static void emitAll(@NonNull String type, @NonNull JsonElement data) {
        bridges.forEach((b) -> b.get().emit(type, data));
    }

    public static void evalAll(@NonNull String script) {
        bridges.forEach((b) -> b.get().eval(script));
    }

}
