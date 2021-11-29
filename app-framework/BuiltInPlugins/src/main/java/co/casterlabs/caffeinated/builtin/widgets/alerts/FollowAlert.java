package co.casterlabs.caffeinated.builtin.widgets.alerts;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;

public class FollowAlert extends Widget {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.follow_alert")
        .withIcon("bell")
        .withCategory(WidgetDetailsCategory.ALERTS)
        .withFriendlyName("Follow Alert");

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
        WidgetSettingsLayout layout = new WidgetSettingsLayout()
            .addSection(
                new WidgetSettingsSection("text_style", "Style")
                    .addItem(WidgetSettingsItem.asUnknown("font", "Font", "Poppins"))
                    .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size", 16, 1, 0, 128))
                    .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"))
            );

        // If the user enables alert audio we show the settings options,
        // otherwise we only show the checkbox.
        // Same for the alert image.
        {
            WidgetSettingsSection alertAudioSection = new WidgetSettingsSection("alert_audio", "Alert Audio")
                .addItem(WidgetSettingsItem.asCheckbox("enable_alert_audio", "Enable Alert Audio", true));

            if (this.safeGetBoolean("alert_audio.enable_alert_audio", true)) {
                alertAudioSection.addItem(
                    WidgetSettingsItem.asUnknown("custom_audio", "Custom Audio")
                );
            }

            layout.addSection(alertAudioSection);
        }

        {
            WidgetSettingsSection alertImageSection = new WidgetSettingsSection("alert_image", "Alert Image")
                .addItem(WidgetSettingsItem.asCheckbox("enable_alert_image", "Show Alert Image", true));

            if (this.safeGetBoolean("alert_image.enable_alert_image", true)) {
                alertImageSection.addItem(
                    WidgetSettingsItem.asDropdown(
                        "alert_image", "Alert Image",
                        "User Avatar",
                        "User Avatar", "Custom Image"
                    )
                );

                if (this.safeGetString("alert_image.alert_image").equals("Custom Image")) {
                    alertImageSection.addItem(
                        WidgetSettingsItem.asUnknown("custom_image", "Custom Image")
                    );
                }
            }

            layout.addSection(alertImageSection);
        }

        this.setSettingsLayout(layout, true);
    }

    private boolean safeGetBoolean(String property, boolean defaultValue) {
        if (this.getSettings().containsKey(property)) {
            return this.getSettings().getBoolean(property);
        } else {
            return defaultValue;
        }
    }

    private String safeGetString(String property) {
        if (this.getSettings().containsKey(property)) {
            return this.getSettings().getString(property);
        } else {
            return "";
        }
    }

}
