package co.casterlabs.caffeinated.app.plugins.impl;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.reflections8.Reflections;

import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.PluginImplementation;
import lombok.NonNull;
import xyz.e3ndr.reflectionlib.helpers.AccessHelper;

public class PluginLoader {

    public static List<CaffeinatedPlugin> loadFile(@NonNull PluginsHandler pluginsInst, @NonNull File file) throws IOException {
        if (file.isFile()) {
            Closeable err = null;

            try {
                URL url = file.toURI().toURL();

                URLClassLoader classLoader = new URLClassLoader(new URL[] {
                        url
                }, PluginLoader.class.getClassLoader());
                err = classLoader;

                return loadFromClassLoader(pluginsInst, classLoader);
            } catch (MalformedURLException e) {
                if (err != null) err.close();

                throw new IOException("Unable to load file", e);
            }
        } else {
            throw new IOException("Target plugin must be a valid file");
        }
    }

    public static List<CaffeinatedPlugin> loadFromClassLoader(@NonNull PluginsHandler pluginsInst, ClassLoader classLoader) throws IOException {
        try {

            Reflections reflections = new Reflections(classLoader);
            Set<Class<?>> types = reflections.getTypesAnnotatedWith(PluginImplementation.class);
            List<CaffeinatedPlugin> plugins = new LinkedList<>();

            // Frees an ungodly amount of ram, Reflections seems to be inefficient.
            reflections = null;
            System.gc();

            if (types.isEmpty()) {
                if (classLoader instanceof Closeable) {
                    ((Closeable) classLoader).close();
                }

                classLoader = null;

                throw new IOException("No implementations are present");
            } else {
                for (Class<?> clazz : types) {
                    if (CaffeinatedPlugin.class.isAssignableFrom(clazz)) {
                        try {
                            CaffeinatedPlugin plugin = (CaffeinatedPlugin) clazz.newInstance();
                            ServiceLoader<Driver> sqlDrivers = ServiceLoader.load(java.sql.Driver.class, classLoader);

                            Field classLoaderField = CaffeinatedPlugin.class.getDeclaredField("classLoader");
                            Field sqlDriversField = CaffeinatedPlugin.class.getDeclaredField("sqlDrivers");
                            Field pluginsField = CaffeinatedPlugin.class.getDeclaredField("plugins");

                            AccessHelper.makeAccessible(classLoaderField);
                            AccessHelper.makeAccessible(sqlDriversField);
                            AccessHelper.makeAccessible(pluginsField);

                            classLoaderField.set(plugin, classLoader);
                            sqlDriversField.set(plugin, sqlDrivers);
                            pluginsField.set(plugin, pluginsInst);

                            // Load in the sql drivers.
                            for (Driver driver : sqlDrivers) {
                                driver.getClass().toString();
                            }

                            plugins.add(plugin);
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException | NoSuchFieldException e) {
                            throw new IOException("Unable to load plugin", e);
                        }
                    }
                }
            }

            return plugins;
        } catch (MalformedURLException e) {
            throw new IOException("Unable to load file", e);
        }
    }

}
