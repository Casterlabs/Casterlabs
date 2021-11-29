package co.casterlabs.caffeinated.builtin;

import co.casterlabs.caffeinated.builtin.widgets.ChatWidget;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.PluginImplementation;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@PluginImplementation
public class CaffeinatedDefaultPlugin extends CaffeinatedPlugin {

    @Override
    public void onInit() {
        this.getLogger().info("I am loaded!");

        this.getPlugins().registerWidget(this, ChatWidget.DETAILS, ChatWidget.class);

        // I spend way too long on this shit.
        FastLogger.logStatic(" _____________");
        FastLogger.logStatic("|     Hi!     |");
        FastLogger.logStatic("| My name is: |");
        FastLogger.logStatic("|‾‾‾‾‾‾‾‾‾‾‾‾‾|");
        FastLogger.logStatic("| %-11s |", this.getName());
        FastLogger.logStatic("|_____________|");
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
