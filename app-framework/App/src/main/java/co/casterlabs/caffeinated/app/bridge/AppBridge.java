package co.casterlabs.caffeinated.app.bridge;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.NonNull;

public abstract class AppBridge {
    protected static Map<String, BridgeValue<?>> queryData = new HashMap<>();
    private static List<WeakReference<AppBridge>> bridges = new ArrayList<>();

    private WeakReference<AppBridge> _$ = new WeakReference<>(this);

    public AppBridge() {
        bridges.add(this._$);
    }

    /* Impl */

    protected abstract void emit0(@NonNull String type, @NonNull JsonElement data);

    protected abstract void eval0(@NonNull String script);

    /* Statics */

    @Override
    protected void finalize() {
        bridges.remove(this._$);
    }

    @Deprecated
    public static void addQueryObject(@NonNull BridgeValue<?> bv) {
        queryData.put(bv.getKey(), bv);
    }

    public static void emit(@NonNull String type, @NonNull JsonElement data) {
        bridges.forEach((b) -> b.get().emit0(type, data));
    }

    public static void eval(@NonNull String script) {
        bridges.forEach((b) -> b.get().eval0(script));
    }

}
