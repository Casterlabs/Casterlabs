package co.casterlabs.caffeinated.pluginsdk;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

public interface CaffeinatedPlugins {

    /**
     * Gets a plugin by it's id. The result is autocast to your type of choice.
     * 
     * @param  id the id of the plugin
     * 
     * @return    the plugin, null if not loaded
     */
    public @Nullable <T extends CaffeinatedPlugin> T getPluginById(@NonNull String id);

    /**
     * Detects whether or not a plugin is loaded.
     * 
     * @param  id the id of the plugin
     * 
     * @return    true if loaded
     */
    public boolean isPluginPresent(@NonNull String id);

}
