package co.casterlabs.caffeinated.builtin;

import co.casterlabs.caffeinated.builtin.widgets.ChatWidget;
import co.casterlabs.caffeinated.builtin.widgets.EmojiRainWidget;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.PluginImplementation;
import lombok.NonNull;

@PluginImplementation
public class CaffeinatedDefaultPlugin extends CaffeinatedPlugin {
    public static final String DEV_ADDRESS = "http://localhost:4088";

    @Override
    public void onInit() {
        // I spend way too long on this shit.
        this.getLogger().info(" _________________");
        this.getLogger().info("|       Hi!       |");
        this.getLogger().info("|   My name is:   |");
        this.getLogger().info("|‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾|");
        this.getLogger().info("|   Casterlabs    |");
        this.getLogger().info("|     Default     |");
        this.getLogger().info("|     Widgets     |");
        this.getLogger().info("|                 |");
        this.getLogger().info(" ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾ ");

        // Interaction
        this.getPlugins().registerWidget(this, ChatWidget.DETAILS, ChatWidget.class);
        this.getPlugins().registerWidget(this, EmojiRainWidget.DETAILS, EmojiRainWidget.class);

        // Alerts
//        this.getPlugins().registerWidget(this, DonationAlert.DETAILS, DonationAlert.class);
//        this.getPlugins().registerWidget(this, FollowAlert.DETAILS, FollowAlert.class);

    }

    @Override
    public void onClose() {

    }

    @Override
    public @NonNull String getName() {
        return "Casterlabs Default Widgets";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.defaultwidgets";
    }

}
