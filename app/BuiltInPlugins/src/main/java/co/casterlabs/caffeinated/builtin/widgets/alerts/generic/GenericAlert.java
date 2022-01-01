package co.casterlabs.caffeinated.builtin.widgets.alerts.generic;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.TTS;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.koi.api.types.events.ChatEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;

public abstract class GenericAlert extends Widget {

    @Override
    public void onInit() {
        this.renderSettingsLayout();
    }

    @Override
    protected void onSettingsUpdate() {
        this.renderSettingsLayout();
    }

    private void renderSettingsLayout() {
        WidgetSettingsLayout layout = this.generateSettingsLayout();

        this.setSettingsLayout(layout, true); // Preserve
    }

    /**
     * Override as neeeded.
     */
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = new WidgetSettingsLayout()
            .addSection(
                new WidgetSettingsSection("style", "Style")
                    .addItem(WidgetSettingsItem.asNumber("duration", "Duration (Seconds)", 15, 1, 0, 60))
                    .addItem(WidgetSettingsItem.asFont("font", "Font", "Poppins"))
                    .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size (px)", 16, 1, 0, 128))
                    .addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right", "Center"))
                    .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"))
                    .addItem(WidgetSettingsItem.asColor("highlight_color", "Highlight Color", "#5bf599"))
            );

        layout.addSection(
            new WidgetSettingsSection("text", "Text")
                .addItem(WidgetSettingsItem.asText("prefix", "Prefix", this.defaultPrefix(), ""))
                .addItem(WidgetSettingsItem.asText("suffix", "Suffix", this.defaultSuffix(), ""))
        );

        if (this.hasTTS()) {
            WidgetSettingsSection ttsSection = new WidgetSettingsSection("tts", "Text To Speech")
                .addItem(WidgetSettingsItem.asCheckbox("enabled", "Enabled", true));

            if (this.settings().getBoolean("tts.enabled", true)) {
                ttsSection
                    .addItem(WidgetSettingsItem.asRange("volume", "Volume", .5, .01, 0, 1))
                    .addItem(WidgetSettingsItem.asDropdown("voice", "Voice", "Brian", TTS.getVoicesAsArray()));

                layout.addButton(
                    new WidgetSettingsButton("skip-tts")
                        .withIcon("skip-forward")
                        .withIconTitle("Skip TTS")
                        .withOnClick(() -> {
                            this.broadcastToAll("skip-tts", new JsonObject());
                        })
                );
            } else {
                // Since tts is disabled, we want to forcibly skip the tts if it's playing.
                this.broadcastToAll("skip-tts", new JsonObject());
            }

            layout.addSection(ttsSection);
        }

        {
            WidgetSettingsSection audioSection = new WidgetSettingsSection("audio", "Alert Audio")
                .addItem(WidgetSettingsItem.asCheckbox("enabled", "Play Audio", true));

            if (this.settings().getBoolean("audio.enabled", true)) {
                audioSection.addItem(WidgetSettingsItem.asUnknown("file", "Audio File", "audio"));
            }

            layout.addSection(audioSection);
        }

        if (!this.hasCustomImageImplementation()) {
            WidgetSettingsSection imageSection = new WidgetSettingsSection("image", "Alert Image")
                .addItem(WidgetSettingsItem.asCheckbox("enabled", "Show Image", true));

            if (this.settings().getBoolean("image.enabled", true)) {
                imageSection.addItem(WidgetSettingsItem.asUnknown("file", "Image File", "image", "video"));
            }

            layout.addSection(imageSection);
        }

        for (WidgetSettingsButton button : this.getButtons()) {
            layout.addButton(button);
        }

        return layout;
    }

    @SneakyThrows
    @Override
    public @Nullable String getWidgetHtml() {
        return CaffeinatedDefaultPlugin.resolveResource("/alert.html");
    }

    public void queueAlert(@NonNull String titleHtml, @Nullable ChatEvent chatEvent, @Nullable String[] customImages, @Nullable String ttsText) {
        String ttsAudio = null;

        // Generate the base64 audio data for TTS if enabled.
        if ((ttsText != null) &&
            this.settings().getBoolean("tts.enabled", true)) {
            try {
                byte[] audioBytes = TTS.getSpeech(this.settings().getString("tts.voice"), ttsText);

                ttsAudio = "data:audio/mp3;base64," + Base64.getMimeEncoder().encodeToString(audioBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Append the prefix and suffix to the title.
        String prefix = WebUtil.escapeHtml(this.settings().getString("text.prefix")).replace(" ", "&nbsp;");
        String suffix = WebUtil.escapeHtml(this.settings().getString("text.suffix")).replace(" ", "&nbsp;");

        if (!prefix.isEmpty()) {
            titleHtml = prefix + ' ' + titleHtml;
        }

        if (!suffix.isEmpty()) {
            titleHtml = titleHtml + ' ' + suffix;
        }

        // Send it on it's way.
        this.broadcastToAll(
            "alert",
            new JsonObject()
                .put("title", titleHtml)
                .put("chatEvent", Rson.DEFAULT.toJson(chatEvent))
                .put("image", this.getImage(customImages))
                .put("audio", this.getAudio())
                .put("ttsAudio", ttsAudio)
        );
    }

    protected List<WidgetSettingsButton> getButtons() {
        return Collections.emptyList();
    }

    protected @Nullable String getImage(@Nullable String[] customImages) {
        return this.settings().getString("image.file");
    }

    protected @Nullable String getAudio() {
        return this.settings().getString("audio.file");
    }

    protected abstract String defaultPrefix();

    protected abstract String defaultSuffix();

    protected abstract boolean hasCustomImageImplementation();

    protected abstract boolean hasTTS();

}
