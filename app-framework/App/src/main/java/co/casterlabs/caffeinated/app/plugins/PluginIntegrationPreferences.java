package co.casterlabs.caffeinated.app.plugins;

import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonClass(exposeAll = true)
public class PluginIntegrationPreferences {
    private Map<String, WidgetSettingsDetails> widgetSettings = new HashMap<>();

    @Getter
    @ToString
    @JsonClass(exposeAll = true)
    public static class WidgetSettingsDetails {
        private String namespace;
        private String name;
        private JsonObject settings = new JsonObject();

        public static WidgetSettingsDetails from(@NonNull Widget widget) {
            WidgetSettingsDetails details = new WidgetSettingsDetails();

            details.namespace = widget.getNamespace();
            details.name = widget.getName();
            details.settings = widget.getSettings();

            return details;
        }

    }

}
