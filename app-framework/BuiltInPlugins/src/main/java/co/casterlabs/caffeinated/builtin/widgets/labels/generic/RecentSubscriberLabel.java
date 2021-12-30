package co.casterlabs.caffeinated.builtin.widgets.labels.generic;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.util.HtmlEscape;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;

public class RecentSubscriberLabel extends GenericLabel implements KoiEventListener {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.recent_subscriber_label")
        .withIcon("users")
        .withCategory(WidgetDetailsCategory.LABELS)
        .withFriendlyName("Recent Subscriber Label");

    private User recentSubscriber;
    private String currHtml = "";

    @Override
    public void onInit() {
        super.onInit();

        this.addKoiListener(this);

        // If this fails then we don't care.
        try {
            JsonElement recentSubscriber = this.getSettings().get("recent_subscriber");

            this.recentSubscriber = Rson.DEFAULT.fromJson(recentSubscriber, User.class);
        } catch (Exception ignored) {}

        this.updateText();
    }

    private void save() {
        this.setSettings(
            this.getSettings()
                .put("recent_subscriber", Rson.DEFAULT.toJson(this.recentSubscriber))
        );
    }

    @Override
    protected List<WidgetSettingsButton> getButtons() {
        return Arrays.asList(
            new WidgetSettingsButton("reset")
                .withIcon("x-circle")
                .withIconTitle("Reset Text")
                .withOnClick(() -> {
                    this.recentSubscriber = null;

                    this.save();
                    this.updateText();
                })
        );
    }

    @Override
    protected void onSettingsUpdate() {
        this.updateText();
    }

    @KoiEventHandler
    public void onSubscription(@Nullable SubscriptionEvent event) {
        if (event.getGiftRecipient() != null) {
            this.recentSubscriber = event.getGiftRecipient();
        } else {
            this.recentSubscriber = event.getSubscriber();
        }

        this.save();
        this.updateText();
    }

    private void updateText() {
        if (this.recentSubscriber == null) {
            this.currHtml = "";
        } else {
            String html = this.recentSubscriber.getDisplayname();

            String prefix = HtmlEscape.escapeHtml(this.getSettings().getString("text.prefix")).replace(" ", "&nbsp;");
            String suffix = HtmlEscape.escapeHtml(this.getSettings().getString("text.suffix")).replace(" ", "&nbsp;");

            if (!prefix.isEmpty()) {
                html = prefix + ' ' + html;
            }

            if (!suffix.isEmpty()) {
                html = html + ' ' + suffix;
            }

            this.currHtml = html;
        }

        for (WidgetInstance instance : this.getWidgetInstances()) {
            try {
                instance.emit("html", JsonObject.singleton("html", this.currHtml));
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void onNewInstance(@NonNull WidgetInstance instance) {
        try {
            instance.emit("html", JsonObject.singleton("html", this.currHtml));
        } catch (IOException ignored) {}
    }

    @Override
    protected boolean hasHighlight() {
        return false;
    }

    @Override
    protected boolean enablePlatformOption() {
        return false;
    }

}
