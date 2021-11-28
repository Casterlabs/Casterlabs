package co.casterlabs.caffeinated.pluginsdk.widgets.settings;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@NonNull
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class WidgetSettingsItem {
    private final String id;
    private final String name;
    private final Object defaultValue;
    private final WidgetSettingsItemType type;

    public static enum WidgetSettingsItemType {

    }

}
