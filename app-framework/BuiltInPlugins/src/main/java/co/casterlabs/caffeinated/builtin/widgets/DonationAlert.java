package co.casterlabs.caffeinated.builtin.widgets;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;

public class DonationAlert extends Widget {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.donation")
        .withIcon("bell")
        .withCategory(WidgetDetailsCategory.ALERTS)
        .withFriendlyName("Donation Alert");

    @Override
    public void onInit() {
        this.updateLayout();
    }

    @Override
    public void onSettingsUpdate() {
        this.updateLayout();
    }

    // Dynamically updates the settings page as the user selects things.
    public void updateLayout() {
        WidgetSettingsSection alertAudioSection = new WidgetSettingsSection("alert_audio", "Alert Audio")
            .addItem(WidgetSettingsItem.asCheckbox("enable_alert_audio", "Enable Alert Audio", true));

        if (this.safeGetBoolean("alert_audio.enable_alert_audio", true)) {
            alertAudioSection.addItem(
                WidgetSettingsItem.asDropdown(
                    "alert_audio", "Alert Audio",
                    "Text-to-Speech",
                    "Custom Audio", "Text-to-Speech"
                )
            );

            if (this.safeGetString("alert_audio.alert_audio", "Custom Audio").equals("Custom Audio")) {
                alertAudioSection.addItem(
                    WidgetSettingsItem.asUnknown("custom_audio", "Custom Audio")
                );
            }
        }

        WidgetSettingsSection alertImageSection = new WidgetSettingsSection("alert_image", "Alert Image")
            .addItem(WidgetSettingsItem.asCheckbox("enable_alert_image", "Show Alert Image", true));

        if (this.safeGetBoolean("alert_image.enable_alert_image", true)) {
            alertImageSection.addItem(
                WidgetSettingsItem.asDropdown(
                    "alert_image", "Alert Image",
                    "Animated Donation Image",
                    "Donation Image", "Animated Donation Image", "Custom Image"
                )
            );

            if (this.safeGetString("alert_image.alert_image", "Custom Image").equals("Custom Image")) {
                alertImageSection.addItem(
                    WidgetSettingsItem.asUnknown("custom_image", "Custom Image")
                );
            }
        }

        this.setSettingsLayout(
            new WidgetSettingsLayout()
                .addSection(
                    new WidgetSettingsSection("text_style", "Style")
                        .addItem(WidgetSettingsItem.asUnknown("font", "Font", "Poppins"))
                        .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size", 16, 1, 0, 128))
                        .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"))
                )
                .addSection(alertAudioSection)
                .addSection(alertImageSection)
                .addSection(
                    new WidgetSettingsSection("moderation", "Moderation")
                        .addItem(WidgetSettingsItem.asCheckbox("hide_bots", "Hide Bots", true))
                        .addItem(WidgetSettingsItem.asCheckbox("hide_naughty_language", "Hide Naughty Language", true))
                )
        );
    }

    private boolean safeGetBoolean(String property, boolean defaultValue) {
        if (this.getSettings().containsKey(property)) {
            return this.getSettings().getBoolean(property);
        } else {
            return defaultValue;
        }
    }

    private String safeGetString(String property, String defaultValue) {
        if (this.getSettings().containsKey(property)) {
            return this.getSettings().getString(property);
        } else {
            return defaultValue;
        }
    }

}
