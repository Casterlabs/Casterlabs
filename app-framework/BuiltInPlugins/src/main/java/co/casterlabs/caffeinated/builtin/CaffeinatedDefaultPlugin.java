package co.casterlabs.caffeinated.builtin;

import co.casterlabs.caffeinated.builtin.widgets.ChatWidget;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.PluginImplementation;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import lombok.NonNull;

@PluginImplementation
public class CaffeinatedDefaultPlugin extends CaffeinatedPlugin {

    @Override
    public void onInit() {
        this.getLogger().info("I am loaded!");

        this.getPlugins().registerWidget(
            this,
            new WidgetDetails()
                .withNamespace("co.casterlabs.chat")
                .withIcon("message-square")
                .withFriendlyName("Chat Widget"),
            ChatWidget.class
        );
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
