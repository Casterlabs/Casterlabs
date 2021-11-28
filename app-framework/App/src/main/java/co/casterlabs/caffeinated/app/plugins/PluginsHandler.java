package co.casterlabs.caffeinated.app.plugins;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugins;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class PluginsHandler implements CaffeinatedPlugins {
    private static final FastLogger logger = new FastLogger();
    private static final @Getter PluginsHandler instance = new PluginsHandler();

    private Map<String, CaffeinatedPlugin> plugins = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T extends CaffeinatedPlugin> T getPluginById(@NonNull String id) {
        CaffeinatedPlugin pl = this.plugins.get(id);

        if (pl == null) {
            return null;
        } else {
            return (T) pl;
        }
    }

    @Override
    public boolean isPluginPresent(@NonNull String id) {
        return this.plugins.containsKey(id);
    }

    public PluginContext loadPluginsFromClassLoader(@NonNull ClassLoader loader) {
        try {
            List<CaffeinatedPlugin> toLoad = PluginLoader.loadFromClassLoader(this, loader);

            return loadPlugins0(toLoad, loader.toString());
        } catch (Exception e) {
            logger.severe("Failed to load plugins from %s", loader);
            logger.exception(e);
            return new PluginContext(Collections.emptyList(), false);
        }
    }

    public PluginContext loadPluginsFromFile(@NonNull File file) {
        try {
            List<CaffeinatedPlugin> toLoad = PluginLoader.loadFile(this, file);
            PluginContext ctx = loadPlugins0(toLoad, file.getName());

            ctx.setFile(file);

            return ctx;
        } catch (Exception e) {
            logger.severe("Failed to load plugins from %s", file.getName());
            logger.exception(e);
            return new PluginContext(Collections.emptyList(), false);
        }
    }

    private PluginContext loadPlugins0(List<CaffeinatedPlugin> toLoad, String source) {
        List<String> pluginIds = new LinkedList<>();
        boolean hasSucceeded = false;

        try {
            for (CaffeinatedPlugin plugin : toLoad) {
                pluginIds.add(plugin.getId());
                this.register(plugin);
            }

            hasSucceeded = true;
            logger.info("Loaded all plugins from %s successfully.", source);
        } catch (Exception e) {
            logger.severe("Failed to load plugins from %s", source);
            logger.exception(e);

            for (String id : new ArrayList<>(pluginIds)) {
                try {
                    this.unregister(id);
                    pluginIds.remove(id);
                } catch (Throwable ignored) {}
            }
        }

        return new PluginContext(pluginIds, hasSucceeded);
    }

    public void register(@NonNull CaffeinatedPlugin plugin) {
        String id = plugin.getId();

        if (this.plugins.containsKey(id)) {
            throw new IllegalStateException(id + " is already registered.");
        } else {
            this.plugins.put(id, plugin);

            logger.info("Loaded plugin %s:%s (%s)", plugin.getName(), plugin.getAuthor(), id);

            plugin.onInit();
        }
    }

    public void unregister(@NonNull String id) {
        if (this.plugins.containsKey(id)) {
            CaffeinatedPlugin plugin = this.plugins.remove(id);
            ClassLoader classLoader = plugin.getClassLoader();

            try {
                plugin.close();
            } catch (Throwable ignored) {}

            try {
                if (classLoader instanceof Closeable) {
                    ((Closeable) classLoader).close();
                }
            } catch (Throwable ignored) {}

            logger.info("Unloaded plugin %s:%s (%s)", plugin.getName(), plugin.getAuthor(), id);

            // Important for the GC sweep to remove the class loader.
            plugin = null;
            classLoader = null;

            System.gc();
        } else {
            throw new IllegalStateException(id + " is not registered.");
        }
    }

    public void unregisterAll() {
        for (String id : this.plugins.keySet().toArray(new String[0])) {
            this.unregister(id);
        }
    }

}
