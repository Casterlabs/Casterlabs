package co.casterlabs.caffeinated.webview.bridge;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.Getter;
import lombok.NonNull;

public class BridgeValue<T> {
    protected List<WeakReference<WebviewBridge>> attachedBridges = new LinkedList<>();

    private @Getter String key;
    private boolean update = true;

    private T value;

    public BridgeValue(@NonNull String key) {
        this.key = key;
    }

    public BridgeValue(@NonNull String key, @Nullable T value) {
        this(key);
        this.set(value);
    }

    public BridgeValue(@NonNull String key, @Nullable T value, boolean allowUpdates) {
        this(key);
        this.update = allowUpdates;
        this.set(value);
    }

    public BridgeValue<T> attachGlobally() {
        WebviewBridge.attachGlobalBridge(this);
        return this;
    }

    /* -------------- */
    /* Get/Set/Update */
    /* -------------- */

    public @Nullable T get() {
        return this.value;
    }

    public BridgeValue<T> set(@Nullable T value) {
        this.value = value;
        this.update();

        return this; // For chaining
    }

    public void update() {
        if (this.update) {
            new AsyncTask(() -> {
                for (WeakReference<WebviewBridge> ref : this.attachedBridges) {
                    ref.get().emit(this.key + ":update", this.getAsJson());
                }
            });
        }
    }

    /* -------------- */
    /* What makes this work */
    /* -------------- */

    public JsonElement getAsJson() {
        return Rson.DEFAULT.toJson(this.value);
    }

    @Override
    public String toString() {
        return this.getAsJson().toString();
    }

    @Override
    public boolean equals(Object other) {
        return (other != null) &&
            (other instanceof BridgeValue) &&
            ((BridgeValue<?>) other).key.equals(this.key);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

}
