package co.casterlabs.caffeinated.app.bridge;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.Getter;
import lombok.NonNull;

public class BridgeValue<T> {
    private @Getter String key;
    private boolean update = true;

    private T value;

    @SuppressWarnings("deprecation")
    public BridgeValue(@NonNull String key) {
        this.key = key;

        AppBridge.addQueryObject(this);
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
                AppBridge.emit(this.key + ":update", this.getAsJson());
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
