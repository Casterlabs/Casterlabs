package co.casterlabs.caffeinated.builtin.widgets;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class ChatWidget extends Widget {

    @Override
    public void onInit() {
        // I spend way too long on this shit.
        FastLogger.logStatic(" _____________");
        FastLogger.logStatic("|     Hi!     |");
        FastLogger.logStatic("| My name is: |");
        FastLogger.logStatic("|‾‾‾‾‾‾‾‾‾‾‾‾‾|");
        FastLogger.logStatic("| %-11s |", this.getName());
        FastLogger.logStatic("|_____________|");

        this.setSettingsLayout(
            new WidgetSettingsLayout()
                .addSection(
                    new WidgetSettingsSection("chat_style", "Style")
                        .addItem(WidgetSettingsItem.asDropdown("chat_direction", "Chat Direction", "down", "down", "up"))
                        .addItem(WidgetSettingsItem.asUnknown("font", "Font", "Poppins"))
                        .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size", 16, 1, 0, 128))
                        .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"))
                        .addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "left", "left", "right"))
                        .addItem(WidgetSettingsItem.asCheckbox("show_donations", "Show Donations", true))
                )
                .addSection(
                    new WidgetSettingsSection("custom_emotes", "Custom Emotes")
                        .addItem(WidgetSettingsItem.asCheckbox("show_betterbrime", "Show BetterBrime Emotes", true))
                        .addItem(WidgetSettingsItem.asCheckbox("show_betterttv", "Show BetterTTV Emotes", true))
                        .addItem(WidgetSettingsItem.asCheckbox("show_casterlabsemotes", "Show Casterlabs Emotes", true))
                )
                .addSection(
                    new WidgetSettingsSection("moderation", "Moderation")
                        .addItem(WidgetSettingsItem.asCheckbox("hide_bots", "Hide Bots", true))
                        .addItem(WidgetSettingsItem.asCheckbox("hide_naughty_language", "Hide Naughty Language", true))
                )
        );
    }

}
