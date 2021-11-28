package co.casterlabs.caffeinated.pluginsdk;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
@JsonClass(exposeAll = true)
public class WidgetDetails {
    private @With @NonNull String namespace;
    private @With @Nullable String icon;
    private @With @NonNull String friendlyName;

    public void validate() {
        assert this.namespace != null : "Namespace cannot be null";
        assert this.icon != null : "Icon cannot be null";
        assert this.friendlyName != null : "Friendly Name cannot be null";
    }

}
