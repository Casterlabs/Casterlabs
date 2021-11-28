package co.casterlabs.caffeinated.pluginsdk.widgets;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

/**
 * See {@link https://feathericons.com/} for icons.
 */
@Value
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class WidgetDetails {
    private @With String namespace;
    private @With @Nullable String icon;
    private @With String friendlyName;

    public WidgetDetails() {
        this.namespace = null;
        this.icon = null;
        this.friendlyName = null;
    }

    public void validate() {
        assert this.namespace != null : "Namespace cannot be null";
        assert this.icon != null : "Icon cannot be null";
        assert this.friendlyName != null : "Friendly Name cannot be null";
    }

}
