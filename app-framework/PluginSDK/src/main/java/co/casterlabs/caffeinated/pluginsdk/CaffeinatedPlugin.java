package co.casterlabs.caffeinated.pluginsdk;

import java.io.Closeable;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.util.Reflective;
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
    /* Don't use this, lol */
    /* ---------------- */

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
