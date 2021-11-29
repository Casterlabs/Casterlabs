package co.casterlabs.caffeinated.pluginsdk.widgets;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.util.Reflective;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@SuppressWarnings("unchecked") // This is for chaining.
public abstract class Widget {
    // These are all set via reflection.
    private String namespace;
    private String id;
    private String name; // This is mutable by the end user.
    private CaffeinatedPlugin plugin;
    private @Getter WidgetDetails details;

    private Runnable pokeOutside;

    private @Nullable WidgetSettingsLayout settingsLayout;
    private @NonNull JsonObject settings;

    @Reflective
    private void setSettings(@Nullable JsonObject newSettings) {
        if (newSettings != null) {
            this.settings = newSettings;
            this.onSettingsUpdate();
        }
    }

    /**
     * @deprecated This is used internally.
     */
    @Deprecated
    public final JsonObject toJson() {
        return new JsonObject()
            .put("namespace", this.namespace)
            .put("id", this.id)
            .put("name", this.name)
            .put("owner", this.plugin.getId())
            .put("details", Rson.DEFAULT.toJson(this.details))
            .put("settingsLayout", Rson.DEFAULT.toJson(this.settingsLayout))
            .put("settings", this.getSettings());
    }

    /* ---------------- */
    /* Abstract Methods */
    /* ---------------- */

    public void onInit() {}

    /**
     * @apiNote By this point the widget will be completely unregistered. You should
     *          speedily destroy whatever you need inorder for your plugin to
     *          properly unload and not leak memory.
     */
    public void onDestroy() {}

    public void onNameUpdate() {}

    public void onSettingsUpdate() {}

    /* ---------------- */
    /* Mutators         */
    /* ---------------- */

    public final synchronized <T extends Widget> T setSettingsLayout(@NonNull WidgetSettingsLayout newSettingsLayout) {
        return this.setSettingsLayout(newSettingsLayout, false);
    }

    public final synchronized <T extends Widget> T setSettingsLayout(@NonNull WidgetSettingsLayout newSettingsLayout, boolean preserveExtraSettings) {
        this.settingsLayout = newSettingsLayout;

        JsonObject oldSettings = this.getSettings();
        JsonObject newSettings = preserveExtraSettings ? this.getSettings() : new JsonObject(); // Clone.

        for (WidgetSettingsSection section : this.settingsLayout.getSections()) {
            for (WidgetSettingsItem item : section.getItems()) {
                String key = String.format("%s.%s", section.getId(), item.getId());

                if (preserveExtraSettings) {
                    JsonElement existingValue = oldSettings.get(key);

                    if (existingValue == null) {
                        JsonElement defaultValue = item.getDefaultValue();

                        newSettings.put(key, defaultValue);
                    } else {
                        newSettings.put(key, existingValue);
                    }
                } else {
                    if (!newSettings.containsKey(key)) {
                        JsonElement defaultValue = item.getDefaultValue();

                        newSettings.put(key, defaultValue);
                    }
                }
            }
        }

        this.settings = newSettings;

        if (this.pokeOutside != null)

        {
            this.pokeOutside.run();
        }

        return (T) this;
    }

    /* ---------------- */
    /* Getters          */
    /* ---------------- */

    public final @Nullable WidgetSettingsLayout getSettingsLayout() {
        return this.settingsLayout;
    }

    @SneakyThrows
    public final JsonObject getSettings() {
        // Convert to string and then reparse as object,
        // Basically one JANKY clone.
        if (this.settings == null) {
            return new JsonObject();
        } else {
            return Rson.DEFAULT.fromJson(this.settings.toString(), JsonObject.class);
        }
    }

    public final String getNamespace() {
        return this.namespace;
    }

    public final String getId() {
        return this.id;
    }

    /**
     * @apiNote This name is editable by the end-user. Do <b>NOT</b> treat as a
     *          unique property, use {@link #getId } instead.
     */
    public final String getName() {
        return this.name;
    }

    // Auto-cast to whatever class you want.
    public final <T extends CaffeinatedPlugin> T getPlugin() {
        return (T) this.plugin;
    }

}
