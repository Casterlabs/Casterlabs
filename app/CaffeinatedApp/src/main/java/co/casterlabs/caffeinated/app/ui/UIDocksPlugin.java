package co.casterlabs.caffeinated.app.ui;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import lombok.NonNull;

public class UIDocksPlugin extends CaffeinatedPlugin {

    @Override
    public void onInit() {
        this.getPlugins().registerWidget(this, StreamChatDock.DETAILS, StreamChatDock.class);
    }

    @Override
    public void onClose() {}

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

}
