package co.casterlabs.caffeinated.builtin.widgets;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Connection;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Listener;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.util.Crypto;
import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;

public class CamWidget extends Widget implements KinokoV1Listener {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.cam_widget")
        .withIcon("video")
        .withCategory(WidgetDetailsCategory.OTHER)
        .withShowDemo(true, 3 / 4d)
        .withFriendlyName("Cam Widget");

    private static final WidgetSettingsLayout LAYOUT = new WidgetSettingsLayout();

    private KinokoV1Connection kinoko = new KinokoV1Connection(this);
    private String channelId;

    private String callerId;

    @Override
    public void onInit() {
        // TODO a way of detecting when we log out of an account.

        this.addKoiListener(new KoiEventListener() {
            @KoiEventHandler
            public void onEvent(KoiEvent e) {
                if (e.getType() != KoiEventType.CATCHUP) {
                    kinoko.send(
                        new JsonObject()
                            .put("type", "KOI_EVENT")
                            .put("event", Rson.DEFAULT.toJson(e))
                            .toString()
                    );
                }
            }
        });

        this.setSettingsLayout(LAYOUT);

        if (this.settings().has("cam.id")) {
            this.channelId = this.settings().getString("cam.id");
        } else {
            this.channelId = new String(Crypto.generateSecureRandomKey());
            this.settings().set("cam.id", this.channelId);
        }

        this.onClose(true);
    }

    @Override
    protected void onDestroy() {
        this.kinoko.close();
    }

    @SneakyThrows
    @Override
    public void onNewInstance(@NonNull WidgetInstance instance) {
        if (instance.getInstanceMode() == WidgetInstanceMode.WIDGET) {
            // Count up how many widgets we have, if we have too many then we want to
            // display an error.
            int count = 0;
            for (WidgetInstance w : this.getWidgetInstances()) {
                if (w.getInstanceMode() == WidgetInstanceMode.WIDGET) {
                    count++;
                }
            }

            instance.on("caller-id", (e) -> {
                this.callerId = e.getAsString();
                this.sendCallerId();
            });

            if (count > 1) {
                this.broadcastToAll("too-many-instances");
            }
        } else {
            instance.emit("id", this.channelId);
        }
    }

    @SneakyThrows
    @Override
    public @Nullable String getWidgetHtml(WidgetInstanceMode mode) {
        if (mode == WidgetInstanceMode.WIDGET) {
            return CaffeinatedDefaultPlugin.resolveResource("/cam.html");
        } else {
            return CaffeinatedDefaultPlugin.resolveResource("/cam-qr.html");
        }
    }

    @Override
    public void onOpen() {}

    @SneakyThrows
    @Override
    public void onMessage(String raw) {
        JsonObject message = Rson.DEFAULT.fromJson(raw, JsonObject.class);
        String type = message.getString("type");

        switch (type) {

            case "INIT": {
                this.kinoko.send(
                    new JsonObject()
                        .put("type", "KOI_HISTORY")
                        .put("history", Rson.DEFAULT.toJson(Caffeinated.getInstance().getKoi().getEventHistory()))
                        .toString()
                );

                this.sendKoiAuth();
                this.sendCallerId();
            }

            case "CHAT": {
                UserPlatform platform = UserPlatform.valueOf(message.getString("platform"));
                String text = message.getString("message");
                String replyTarget = message.getString("replyTarget");
                Caffeinated.getInstance().getKoi().sendChat(platform, text, KoiChatterType.CLIENT, replyTarget, true);
            }

            case "UPVOTE": {
                UserPlatform platform = UserPlatform.valueOf(message.getString("platform"));
                String messageId = message.getString("messageId");
                Caffeinated.getInstance().getKoi().upvoteChat(platform, messageId);
            }

            case "DELETE": {
                UserPlatform platform = UserPlatform.valueOf(message.getString("platform"));
                String messageId = message.getString("messageId");
                Caffeinated.getInstance().getKoi().deleteChat(platform, messageId, true);
            }

        }
    }

    public void sendCallerId() {
        this.kinoko.send(
            new JsonObject()
                .put("type", "CALLER_ID")
                .put("id", this.callerId)
                .toString()
        );
    }

    public void sendKoiAuth() {
        this.kinoko.send(
            new JsonObject()
                .put("type", "KOI_AUTH")
                .put("platforms", Rson.DEFAULT.toJson(Caffeinated.getInstance().getKoi().getSignedInPlatforms()))
                .toString()
        );
    }

    @SneakyThrows
    @Override
    public void onClose(boolean remote) {
        if (remote) {
            this.kinoko.connect("casterlabs_camshare:" + this.channelId, true, false);
        }
    }

}
