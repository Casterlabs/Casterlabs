package co.casterlabs.caffeinated.builtin.widgets.labels.generic;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.util.HtmlEscape;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;

public class SubscriberCountLabel extends GenericLabel implements KoiEventListener {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.subscriber_count_label")
        .withIcon("users")
        .withCategory(WidgetDetailsCategory.LABELS)
        .withFriendlyName("Subscriber Count Label");

    private String currHtml = "";

    @Override
    public void onInit() {
        super.onInit();

        this.addKoiListener(this);
        this.onUserUpdate(null);
    }

    @Override
    protected void onSettingsUpdate() {
        this.onUserUpdate(null);
    }

    @KoiEventHandler
    public void onUserUpdate(@Nullable UserUpdateEvent _ignored) {
        UserPlatform platform = this.getSelectedPlatform();

        if (platform != null) {
            this.updateText(Koi.getUserStates().get(platform).getStreamer().getSubCount());
        }
    }

    private void updateText(long subscriberCount) {
        if (subscriberCount == -1) {
            subscriberCount = 0; // TODO detect if the platform supports subscribers.
        }

        String html = String.valueOf(subscriberCount);

        String prefix = HtmlEscape.escapeHtml(this.settings().getString("text.prefix")).replace(" ", "&nbsp;");
        String suffix = HtmlEscape.escapeHtml(this.settings().getString("text.suffix")).replace(" ", "&nbsp;");

        if (!prefix.isEmpty()) {
            html = prefix + ' ' + html;
        }

        if (!suffix.isEmpty()) {
            html = html + ' ' + suffix;
        }

        this.currHtml = html;

        this.broadcastToAll("html", JsonObject.singleton("html", this.currHtml));
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
        return true;
    }

}
