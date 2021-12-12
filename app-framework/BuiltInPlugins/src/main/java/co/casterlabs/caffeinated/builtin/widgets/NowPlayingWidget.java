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

public class NowPlayingWidget extends Widget {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.now_playing")
        .withIcon("music")
        .withCategory(WidgetDetailsCategory.OTHER)
        .withFriendlyName("Now Playing Widget");

    private static final WidgetSettingsLayout LAYOUT = new WidgetSettingsLayout()
        .addSection(
            new WidgetSettingsSection("style", "Style")
                .addItem(WidgetSettingsItem.asDropdown("background_style", "Background Style", "Blur", "Blur", "Clear", "Solid"))
                .addItem(WidgetSettingsItem.asDropdown("image_style", "Image Location", "Left", "Left", "Right", "None"))
        );

    @Override
    public void onInit() {
        this.setSettingsLayout(LAYOUT);
    }

    @SneakyThrows
    @Override
    public @Nullable String getWidgetHtml() {
        if (CaffeinatedPlugin.isDevEnvironment()) {
            return WebUtil.sendHttpRequest(new Request.Builder().url(CaffeinatedDefaultPlugin.DEV_ADDRESS + "/now-playing.html"));
        } else {
            return FileUtil.loadResource("widgets/chat.html");
        }
    }

}
