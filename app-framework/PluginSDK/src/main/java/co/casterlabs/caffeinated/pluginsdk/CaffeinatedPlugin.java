package co.casterlabs.caffeinated.pluginsdk;

import java.io.Closeable;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.util.Reflective;
import co.casterlabs.caffeinated.util.async.Promise;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class CaffeinatedPlugin implements Closeable {
    private final @Getter FastLogger logger = new FastLogger(this.getName());

    // Helpers so the plugin can interract with the framework.
    private @Reflective @Getter CaffeinatedPlugins plugins;

    private @Reflective @Nullable ClassLoader classLoader;
    private @Reflective ServiceLoader<Driver> sqlDrivers;

    private @Reflective List<String> widgetNamespaces = new LinkedList<>();
    private @Reflective List<Widget> widgets = new LinkedList<>();

    private @Reflective Set<KoiEventListener> koiListeners = new HashSet<>();

    /**
     * @deprecated While this is used internally, plugins can use it as well for
     *             internal event shenanigans. Though, it is important to note that
     *             it will <b>NOT</b> bubble to other plugins.
     * 
     * @return     A completion promise, it has no result and is only useful if you
     *             need to ensure the listeners fire before you continue executing.
     *             See {@link Promise#await()} or
     *             {@link Promise#then(java.util.function.Consumer)}
     */
    @Deprecated
    public Promise<Void> fireKoiEventListeners(@NonNull KoiEvent event) {
        return new Promise<Void>(() -> {
            for (KoiEventListener listener : new ArrayList<>(this.koiListeners)) {
                try {
                    KoiEventUtil.reflectInvoke(listener, event);
                } catch (Throwable t) {
                    this.logger.severe("An error occurred whilst processing Koi event:");
                    this.logger.exception(t);
                }
            }

            return null;
        });
    }

    /* ---------------- */
    /* Helpers          */
    /* ---------------- */

    /**
     * @apiNote Calling {@link #addKoiListener(KoiEventListener)} multiple times
     *          with the same listener won't register it multiple times. The
     *          internal implementation is a {@link HashSet}.
     */
    public void addKoiListener(@NonNull KoiEventListener listener) {
        this.koiListeners.add(listener);
    }

    public void removeKoiListener(@NonNull KoiEventListener listener) {
        this.koiListeners.remove(listener);
    }

    /* ---------------- */
    /* Overrides        */
    /* ---------------- */

    /**
     * @apiNote {@link #onInit()} is always called <b>before</b> the plugin is fully
     *          registered.
     */
    public abstract void onInit();

    /**
     * @apiNote {@link #onClose()} is always called <b>after</b> the plugin has been
     *          unregistered.
     */
    public abstract void onClose();

    public @Nullable String getVersion() {
        return null;
    }

    public @Nullable String getAuthor() {
        return null;
    }

    public abstract @NonNull String getName();

    public abstract @NonNull String getId();

    /**
     * A helper to get resources out of the plugin to a widget.
     * 
     * @implNote            plugins should override this and return whatever data
     *                      they want.
     * 
     * @param    resourceId the id of the resource
     * 
     * @return              null if no data is found.
     */
    public @Nullable byte[] getResource(@NonNull String resourceId) {
        return null;
    }

    /* ---------------- */
    /* Getters          */
    /* ---------------- */

    public final @Nullable ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public final List<Widget> getWidgets() {
        return Collections.unmodifiableList(this.widgets);
    }

    /* ---------------- */
    /* Don't use these, lol */
    /* ---------------- */

    /**
     * @deprecated This is used internally.
     */
    @Deprecated
    public final JsonObject toJson() {
        return new JsonObject()
            .put("version", this.getVersion())
            .put("author", this.getAuthor())
            .put("name", this.getName())
            .put("id", this.getId());
    }

    /**
     * @deprecated Do not use, you will destroy your widget on accident. You have
     *             been warned.
     */
    @Deprecated
    @Override
    public final void close() {
        this.onClose();

        // Unload the SQL Drivers.
        for (Driver driver : this.sqlDrivers) {
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException ignored) {}
        }
    }

}
