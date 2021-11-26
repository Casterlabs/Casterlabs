package co.casterlabs.caffeinated.pluginsdk.widgets;

import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettings;
import lombok.NonNull;

public interface Widget {

    public Widget setSettings(@NonNull WidgetSettings newSettings);

}
