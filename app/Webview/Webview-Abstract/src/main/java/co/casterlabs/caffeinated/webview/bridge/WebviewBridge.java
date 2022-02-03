package co.casterlabs.caffeinated.webview.bridge;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.casterlabs.caffeinated.util.DualConsumer;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class WebviewBridge {
    private static Map<String, BridgeValue<?>> globalQueryData = new HashMap<>();
    private static List<WeakReference<WebviewBridge>> bridges = new ArrayList<>();

    @Getter
    @Setter
    @Deprecated
    protected BridgeHandle handle;

    private WeakReference<WebviewBridge> $ref = new WeakReference<>(this);

    public WebviewBridge() {
        bridges.add(this.$ref);
    }

    public void attachBridge(@NonNull BridgeValue<?> bv) {
        handle.personalQueryData.put(bv.getKey(), bv);
        bv.attachedBridges.add(this.$ref);
    }

    protected Map<String, BridgeValue<?>> getQueryData() {
        Map<String, BridgeValue<?>> combined = new HashMap<>();

        combined.putAll(handle.personalQueryData);
        combined.putAll(globalQueryData);

        return combined;
    }

    @Override
    protected void finalize() {
        bridges.remove(this.$ref);

        for (BridgeValue<?> bv : handle.personalQueryData.values()) {
            bv.attachedBridges.remove(this.$ref);
        }
    }

    public void setOnEvent(DualConsumer<String, JsonObject> onEvent) {
        handle.onEvent = onEvent;
    }

    /* Impl */

    public abstract void emit(@NonNull String type, @NonNull JsonElement data);

    public abstract void eval(@NonNull String script);

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

    public static class BridgeHandle {
        private @Getter DualConsumer<String, JsonObject> onEvent;
        private Map<String, BridgeValue<?>> personalQueryData = new HashMap<>();

    }

}
