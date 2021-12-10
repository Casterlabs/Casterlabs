package co.casterlabs.caffeinated.pluginsdk.widgets;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.Getter;
import lombok.NonNull;

public abstract class WidgetInstance implements Closeable {
    private @Getter boolean isDemo;
    private @Getter String connectionId;

    private MultiValuedMap<String, Consumer<JsonElement>> eventHandlers = new HashSetValuedHashMap<>();
    private Set<Runnable> onCloseHandlers = new HashSet<>();

    public WidgetInstance(boolean isDemo, String connectionId) {
        this.isDemo = isDemo;
        this.connectionId = connectionId;
    }

    /* ------------ */
    /* Used by the IMPL */
    /* ------------ */

    protected void broadcast(@NonNull String type, @NonNull JsonElement message) {
        this.eventHandlers
            .get(type)
            .forEach((c) -> c.accept(message));
    }

    protected void onClose() {
        this.onCloseHandlers
            .forEach((r) -> r.run());
    }

    /* ------------ */
    /* Events       */
    /* ------------ */

    public void on(@NonNull String type, @NonNull Consumer<JsonElement> handler) {
        this.eventHandlers.put(type, handler);
    }

    public void onClose(@NonNull Runnable handler) {
        this.onCloseHandlers.add(handler);
    }

    public abstract void emit(@NonNull String type, @NonNull JsonElement message) throws IOException;

    public abstract @NonNull String getRemoteIpAddress();

    // Convenience method for #emit(String, JsonElement)
    public void emit(@NonNull String type, @NonNull String message) throws IOException {
        this.emit(type, new JsonString(message));
    }

    /* ------------ */
    /* Misc         */
    /* ------------ */

    public abstract void onSettingsUpdate();

    public abstract void onKoiEvent(@NonNull KoiEvent event) throws IOException;

}
