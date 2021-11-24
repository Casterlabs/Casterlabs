package co.casterlabs.caffeinated.pluginsdk;

import java.io.Closeable;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ServiceLoader;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class CaffeinatedPlugin implements Closeable {
    private final @Getter FastLogger logger = new FastLogger(this.getName());

    // Helpers so the plugin can interract with the framework.
    private @Getter CaffeinatedPlugins plugins;

    private @Nullable ClassLoader classLoader;
    private ServiceLoader<Driver> sqlDrivers;

    public abstract void onInit();

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

    public final @Nullable ClassLoader getClassLoader() {
        return this.classLoader;
    }

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
