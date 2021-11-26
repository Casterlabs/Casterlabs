package co.casterlabs.caffeinated.pluginsdk.widgets.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@NonNull
@Accessors(chain = true)
@JsonClass(exposeAll = true)
public class WidgetSettings {
    private List<WidgetSettingsSection> sections = new LinkedList<>();

    public List<WidgetSettingsSection> getSections() {
        return Collections.unmodifiableList(this.sections);
    }

    public WidgetSettings setSections(@NonNull WidgetSettingsSection... section) {
        this.setSections(Arrays.asList(section));
        return this;
    }

    public WidgetSettings setSections(@NonNull List<WidgetSettingsSection> sections) {
        for (WidgetSettingsSection section : sections) {
            assert section == null : "NULL is not a valid WidgetSettingsSection.";
        }

        this.sections = new LinkedList<>(sections);
        return this;
    }

    public WidgetSettings addSection(@NonNull WidgetSettingsSection section) {
        this.sections.add(section);
        return this;
    }

    public WidgetSettings removeSection(@NonNull WidgetSettingsSection section) {
        this.sections.remove(section);
        return this;
    }

}
