package co.casterlabs.caffeinated.builtin.chat;

import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.PluginImplementation;
import lombok.NonNull;

@PluginImplementation
public class CaffeinatedDefaultPlugin extends CaffeinatedPlugin {

    @Override
    public void onInit() {
        this.getLogger().info("I am loaded!");
    }

    @Override
    public void onClose() {

    }

    @Override
    public @NonNull String getName() {
        return "Chat Widget";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.chatwidget";
    }

}
