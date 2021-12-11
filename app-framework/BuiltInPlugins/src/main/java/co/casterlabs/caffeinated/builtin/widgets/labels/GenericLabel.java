package co.casterlabs.caffeinated.builtin.widgets.labels;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.util.FileUtil;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import lombok.SneakyThrows;
import okhttp3.Request;

public abstract class GenericLabel extends Widget implements KoiEventListener {
    private static String[] platforms;

    private boolean wasMultiPlatform;

    static {
        List<String> platformsList = new ArrayList<>();

        for (UserPlatform platform : UserPlatform.values()) {
            if (platform == UserPlatform.CASTERLABS_SYSTEM) {
                continue;
            }

            String name = platform.name().toLowerCase();

            platformsList.add(name.substring(0, 1).toUpperCase() + name.substring(1));
        }

        platforms = platformsList.toArray(new String[0]);
    }

    @Override
    public void onInit() {
        this.renderSettingsLayout();

        this.addKoiListener(this);
    }

    private void renderSettingsLayout() {
        WidgetSettingsLayout layout = new WidgetSettingsLayout();

        {
            WidgetSettingsSection textStyle = new WidgetSettingsSection("text_style", "Style")
//              .addItem(WidgetSettingsItem.asUnknown("font", "Font", "Poppins"))
                .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size (px)", 16, 1, 0, 128))
                .addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right", "Center"))
                .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"));

            if (this.hasHighlight()) {
                textStyle.addItem(WidgetSettingsItem.asColor("highlight_color", "Highlight Color", "#5bf599"));
            }

            layout.addSection(textStyle);
        }

        layout.addSection(
            new WidgetSettingsSection("text", "Text")
                .addItem(WidgetSettingsItem.asText("prefix", "Prefix", "", ""))
                .addItem(WidgetSettingsItem.asText("suffix", "Suffix", "", ""))
        );

        if (this.isMultiPlatform()) {
            layout.addSection(
                new WidgetSettingsSection("platform", "Platform")
                    .addItem(WidgetSettingsItem.asDropdown("platform", "Platform", platforms[0], platforms))
            );
        }

        this.setSettingsLayout(layout, true); // Preserve
    }

    @SneakyThrows
    @Override
    public @Nullable String getWidgetHtml() {
        if (CaffeinatedPlugin.isDevEnvironment()) {
            return WebUtil.sendHttpRequest(new Request.Builder().url(CaffeinatedDefaultPlugin.DEV_ADDRESS + "/text.html"));
        } else {
            return FileUtil.loadResource("widgets/text.html");
        }
    }

    @KoiEventHandler
    public void GenericLabel_onUserUpdate(UserUpdateEvent event) {
        boolean isMultiPlatform = this.isMultiPlatform();

        if (isMultiPlatform != this.wasMultiPlatform) {
            this.wasMultiPlatform = isMultiPlatform;
            this.renderSettingsLayout();
        }
    }

    public @Nullable UserPlatform getSelectedPlatform() {
        if (Koi.isSignedOut()) {
            return null;
        } else {
            UserPlatform platform = Koi.getFirstSignedInPlatform();

            if (this.isMultiPlatform()) {
                try {
                    UserPlatform selectedPlatform = UserPlatform.valueOf(this.getSettings().getString("platform.platform").toUpperCase());

                    // Make sure that platform is signed in.
                    if (Koi.getUserStates().containsKey(selectedPlatform)) {
                        platform = selectedPlatform;
                    }
                } catch (Exception e) {
                    // INVALID VALUE.
                }
            }

            return platform;
        }
    }

    protected abstract boolean hasHighlight();

    protected boolean isMultiPlatform() {
        return Koi.getUserStates().size() > 1;
    }

}
