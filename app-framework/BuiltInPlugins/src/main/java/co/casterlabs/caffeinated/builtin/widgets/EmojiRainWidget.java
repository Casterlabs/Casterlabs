package co.casterlabs.caffeinated.builtin.widgets;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.util.FileUtil;
import co.casterlabs.caffeinated.util.WebUtil;
import lombok.SneakyThrows;
import okhttp3.Request;

public class EmojiRainWidget extends Widget {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.emojirain_widget")
        .withIcon("smile")
        .withCategory(WidgetDetailsCategory.INTERACTION)
        .withFriendlyName("Emoji Rain");

    private static final WidgetSettingsLayout LAYOUT = new WidgetSettingsLayout()
        .addSection(
            new WidgetSettingsSection("rain_settings", "Rain Settings")
                .addItem(WidgetSettingsItem.asNumber("life_time", "Time on Screen (Seconds)", 15, 1, 0, 128))
                .addItem(WidgetSettingsItem.asNumber("max_emojis", "Max Emojis on Screen", 1000, 1, 2, 10000))
                .addItem(WidgetSettingsItem.asNumber("size", "Emoji Size (px)", 32, 1, 0, 128))
                .addItem(WidgetSettingsItem.asNumber("speed", "Fall Speed", 25, 1, 0, 100)) // TODO make this a slider.
        );

    @Override
    public void onInit() {
        this.setSettingsLayout(LAYOUT);
    }

    @SneakyThrows
    @Override
    public @Nullable String getWidgetHtml() {
        if (CaffeinatedPlugin.isDevEnvironment()) {
            return WebUtil.sendHttpRequest(new Request.Builder().url(CaffeinatedDefaultPlugin.DEV_ADDRESS + "/rain.html"));
        }
        return FileUtil.loadResource("widgets/rain.html");
    }

}
