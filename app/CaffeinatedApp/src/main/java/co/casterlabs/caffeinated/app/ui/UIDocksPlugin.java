package co.casterlabs.caffeinated.app.ui;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.caffeinated.webview.bridge.BridgeValue;
import co.casterlabs.caffeinated.webview.bridge.WebviewBridge;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.NonNull;

// This is so nasty, I love it.
public class UIDocksPlugin extends CaffeinatedPlugin {
    private static BridgeShim bridge;

    @SuppressWarnings("deprecation")
    @Override
    public void onInit() {
        this.getPlugins().registerWidget(this, StreamChatDock.DETAILS, StreamChatDock.class);

        bridge = new BridgeShim();
        bridge.mergeWith(CaffeinatedApp.getInstance().getAppBridge());
    }

    @Override
    public void onClose() {

    }

    @Override
    public @NonNull String getName() {
        return "Casterlabs UI Docks";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.uidocks";
    }

    public static class StreamChatDock extends Widget {
        public static final WidgetDetails DETAILS = new WidgetDetails()
            .withNamespace("co.casterlabs.dock.stream_chat")
            .withCategory(WidgetDetailsCategory.INTERACTION)
            .withType(WidgetType.DOCK)
            .withFriendlyName("Stream Chat");

        private static final List<String> EVENT_TYPES = Arrays.asList(
            "ui:save_chat_viewer_preferences",
            "koi:chat_send",
            "koi:chat_delete",
            "koi:chat_upvote",
            "auth:update"
        );

        @Override
        public void onNewInstance(@NonNull WidgetInstance instance) {
            for (String event : EVENT_TYPES) {
                instance.on(event, (c) -> {
                    bridge.fireEvent(event, c);
                });
            }

            JsonElement chatViewerPreferences = bridge.getQueryData().get("ui:chatViewerPreferences").getAsJson();
            this.broadcastToAll("ui:chatViewerPreferences:update", chatViewerPreferences);
        }

        @Override
        public @Nullable String getWidgetHtml(WidgetInstanceMode mode) {
            return String.format(
                "<!DOCTYPE html>" +
                    "<html>" +
                    "<script> location.href = `%s/popout/stream-chat`; </script>" +
                    "</html",
                CaffeinatedApp.getInstance().getAppLoopbackUrl()
            );
        }

    }

    public class BridgeShim extends WebviewBridge {

        @Override
        public Map<String, BridgeValue<?>> getQueryData() {
            return super.getQueryData();
        }

        public void fireEvent(String type, JsonElement data) {
            this.onEvent.accept(type, data.getAsObject());
        }

        @Override
        protected void emit0(@NonNull String type, @NonNull JsonElement data) {
            for (Widget w : getWidgets()) {
                w.broadcastToAll(type, data);
            }
        }

        @Override
        protected void eval0(@NonNull String script) {
            for (Widget w : getWidgets()) {
                w.broadcastToAll("__eval", new JsonString(script));
            }
        }

    }
}
