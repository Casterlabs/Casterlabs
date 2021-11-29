package co.casterlabs.caffeinated.pluginsdk.widgets.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.NonNull;

@Getter
@NonNull
@JsonClass(exposeAll = true)
public class WidgetSettingsLayout {
    private List<WidgetSettingsSection> sections = new LinkedList<>();
    private boolean allowWidgetPreview;

    /**
     * By allowing widget preview it will embed your widget on the settings page. In
     * your widget code you can detect whether or not it's in preview mode like
     * this:
     * 
     * <pre>
     * if (Widget.isInPreviewMode()) {
     *     // Do what you want
     * }
     * </pre>
     */
    public WidgetSettingsLayout setAllowWidgetPreview(boolean allowWidgetPreview) {
        this.allowWidgetPreview = allowWidgetPreview;
        return this;
    }

    public List<WidgetSettingsSection> getSections() {
        return Collections.unmodifiableList(this.sections);
    }

    public WidgetSettingsLayout setSections(@NonNull WidgetSettingsSection... section) {
        this.setSections(Arrays.asList(section));
        return this;
    }

    public WidgetSettingsLayout setSections(@NonNull List<WidgetSettingsSection> sections) {
        for (WidgetSettingsSection section : sections) {
            assert section == null : "NULL is not a valid WidgetSettingsSection.";
        }

        this.sections = new LinkedList<>(sections);
        return this;
    }

    public WidgetSettingsLayout addSection(@NonNull WidgetSettingsSection section) {
        this.sections.add(section);
        return this;
    }

    public WidgetSettingsLayout removeSection(@NonNull WidgetSettingsSection section) {
        this.sections.remove(section);
        return this;
    }

}
