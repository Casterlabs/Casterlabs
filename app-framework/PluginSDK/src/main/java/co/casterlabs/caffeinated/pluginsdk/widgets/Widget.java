package co.casterlabs.caffeinated.pluginsdk.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.util.Reflective;
import co.casterlabs.caffeinated.util.async.Promise;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import co.casterlabs.rakurai.json.validation.JsonValidationException;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@SuppressWarnings("unchecked") // This is for chaining.
public abstract class Widget {
    // <All set by reflection>
    private @JsonField String namespace;
    private @JsonField String id;
    private @JsonField String name; // This is mutable by the end user.

    private CaffeinatedPlugin plugin;

    private @JsonField @Getter WidgetType type = WidgetType.WIDGET; // TODO Add more.
    private @JsonField WidgetDetails details;

    private @Reflective Runnable pokeOutside;
    // </All set by reflection>

    private @JsonField @Nullable WidgetSettingsLayout settingsLayout;
    private @JsonField @NonNull JsonObject settings;

    private @Reflective Set<KoiEventListener> koiListeners = new HashSet<>();

    private List<WidgetInstance> widgetInstances = new LinkedList<>();

    /* ---------------- */
    /* Serialization    */
    /* ---------------- */

    @JsonSerializationMethod("owner")
    private JsonElement $serialize_owner() {
        return Rson.DEFAULT.toJson(this.plugin.getId());
    }

    @JsonValidate
    private void validate() throws JsonValidationException {
        throw new JsonValidationException("You cannot deserialize into a widget.");
    }

    /* ---------------- */
    /* Internals        */
    /* ---------------- */

    @Reflective
    private void cleanlyDestroy() {
        this.widgetInstances.forEach((w) -> {
            try {
                w.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.onDestroy();
    }

    /* ---------------- */
    /* Framework        */
    /* ---------------- */

    /**
     * @deprecated While this is used internally, plugins can use it as well for
     *             internal event shenanigans. Though, it is important to note that
     *             it will <b>NOT</b> bubble to the parent plugin or other plugins.
     * 
     * @return     A completion promise, it has no result and is only useful if you
     *             need to ensure the listeners fire before you continue executing.
     *             See {@link Promise#await()} or
     *             {@link Promise#then(java.util.function.Consumer)}
     */
    @Deprecated
    public final Promise<Void> fireKoiEventListeners(@NonNull KoiEvent event) {
        return new Promise<Void>(() -> {
            for (KoiEventListener listener : new ArrayList<>(this.koiListeners)) {
                try {
                    KoiEventUtil.reflectInvoke(listener, event);
                } catch (Throwable t) {
                    FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst processing Koi event:");
                    FastLogger.logException(t);
                }
            }

            for (WidgetInstance widgetInstance : this.getWidgetInstances()) {
                try {
                    widgetInstance.onKoiEvent(event);
                } catch (Throwable t) {
                    FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst processing Koi event:");
                    FastLogger.logException(t);
                }
            }

            return null;
        });
    }

    /* ---------------- */
    /* Koi              */
    /* ---------------- */

    /**
     * @apiNote Calling {@link #addKoiListener(KoiEventListener)} multiple times
     *          with the same listener won't register it multiple times. The
     *          internal implementation is a {@link HashSet}.
     */
    public final void addKoiListener(@NonNull KoiEventListener listener) {
        this.koiListeners.add(listener);
    }

    public final void removeKoiListener(@NonNull KoiEventListener listener) {
        this.koiListeners.remove(listener);
    }

    /* ---------------- */
    /* Settings         */
    /* ---------------- */

    @Reflective
    private void $onSettingsUpdate(@Nullable JsonObject newSettings) {
        if (newSettings != null) {
            this.settings = newSettings;
            this.onSettingsUpdate();

            for (WidgetInstance widgetInstance : this.getWidgetInstances()) {
                try {
                    widgetInstance.onSettingsUpdate();
                } catch (Throwable t) {}
            }
        }
    }

    /* ---------------- */
    /* Abstract Methods */
    /* ---------------- */

    public void onInit() {}

    /**
     * @apiNote  By this point the widget will be completely unregistered. You
     *           should speedily destroy whatever you need inorder for your plugin
     *           to properly unload and not leak memory.
     * 
     * @implNote This is called internally, hence the <b>protected</b> modified.
     */
    protected void onDestroy() {}

    public void onNameUpdate() {}

    protected void onSettingsUpdate() {}

    public void onNewInstance(@NonNull WidgetInstance instance) {}

    public @Nullable String getWidgetHtml() {
        return null;
    }

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

        if (this.pokeOutside != null) {
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

    public final <T extends Widget> T setSettings(@NonNull JsonObject newSettings) {
        this.settings = newSettings;

        if (this.pokeOutside != null) {
            this.pokeOutside.run();
        }

        return (T) this;
    }

    public final String getNamespace() {
        return this.namespace;
    }

    public final String getId() {
        return this.id;
    }

    public final List<WidgetInstance> getWidgetInstances() {
        return new ArrayList<>(this.widgetInstances);
    }

    public final WidgetDetails getWidgetDetails() {
        return this.details;
    }

    /**
     * @apiNote This name is editable by the end-user. Do <b>NOT</b> treat as a
     *          unique property, use {@link #getId() } for that instead.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * The result is auto-cast to whatever type you want.
     */
    public final <T extends CaffeinatedPlugin> T getPlugin() {
        return (T) this.plugin;
    }

}
