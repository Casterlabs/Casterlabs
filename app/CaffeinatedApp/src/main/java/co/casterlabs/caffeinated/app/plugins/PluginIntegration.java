package co.casterlabs.caffeinated.app.plugins;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.bridge.BridgeValue;
import co.casterlabs.caffeinated.app.plugins.PluginIntegrationPreferences.WidgetSettingsDetails;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationClickWidgetSettingsButtonEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationCopyWidgetUrlEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationCreateWidgetEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationDeleteWidgetEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationEditWidgetSettingsEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationEventType;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationRenameWidgetEvent;
import co.casterlabs.caffeinated.app.plugins.impl.PluginContext;
import co.casterlabs.caffeinated.app.plugins.impl.PluginsHandler;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.ui.UIBackgroundColor;
import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.util.ClipboardUtil;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import lombok.SneakyThrows;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@Getter
public class PluginIntegration {
    private static EventHandler<AppPluginIntegrationEventType> handler = new EventHandler<>();
    private static final File pluginsDir = new File(CaffeinatedApp.appDataDir, "plugins");

    private PluginsHandler plugins = new PluginsHandler();
    private List<PluginContext> contexts = new ArrayList<>();

    private BridgeValue<PluginsBridgeObject> bridge = new BridgeValue<>("plugins", new PluginsBridgeObject());

    @SuppressWarnings("unused")
    @JsonClass(exposeAll = true)
    private static class PluginsBridgeObject {
        private List<CaffeinatedPlugin> loadedPlugins;
        private List<WidgetDetails> creatableWidgets;
        private List<Widget> widgets;

    }

    public PluginIntegration() {
        handler.register(this);

        pluginsDir.mkdir();
    }

    @SneakyThrows
    public void init() {
        // Load the built-in widgets.
        CaffeinatedPlugin defaultPlugin = new CaffeinatedDefaultPlugin();

        ReflectionLib.setValue(defaultPlugin, "plugins", this.plugins);
        this.contexts.add(this.plugins.unsafe_loadPlugins(Arrays.asList(defaultPlugin), "Caffeinated"));

        for (File file : pluginsDir.listFiles()) {
            String fileName = file.getName();

            if (file.isFile() &&
                fileName.endsWith(".jar") &&
                !fileName.startsWith("__")) {
                try {
                    this.contexts.add(
                        this.plugins.loadPluginsFromFile(file)
                    );
                    FastLogger.logStatic(LogLevel.INFO, "Loaded %s", fileName);
                } catch (Exception e) {
                    FastLogger.logStatic(LogLevel.SEVERE, "Unable to load %s as a plugin, make sure that it's *actually* a plugin!", fileName);
                    FastLogger.logException(e);
                }
            }
        }

        for (WidgetSettingsDetails details : CaffeinatedApp.getInstance().getPluginIntegrationPreferences().get().getWidgetSettings()) {
            try {
                // Reconstruct the widget.
                this.plugins.createWidget(details.getNamespace(), details.getId(), details.getName(), details.getSettings());
            } catch (AssertionError | SecurityException | IllegalArgumentException e) {
                if ("A factory associated to that widget is not registered.".equals(e.getMessage())) {
                    // We can safely ignore it.
                    // TODO let the user know that the widget could not be found.
                    FastLogger.logStatic(LogLevel.WARNING, "Unable to create missing widget: %s (%s)", details.getName(), details.getNamespace());
                } else {
                    e.printStackTrace();
                }
            }
        }

        this.save();
    }

    public void save() {
        PreferenceFile<PluginIntegrationPreferences> prefs = CaffeinatedApp.getInstance().getPluginIntegrationPreferences();

        List<WidgetSettingsDetails> widgetSettings = new LinkedList<>();
        for (Widget widget : this.plugins.getWidgets()) {
            widgetSettings.add(WidgetSettingsDetails.from(widget));
        }

        prefs.get().setWidgetSettings(widgetSettings);
        prefs.save();

        this.updateBridgeData();
    }

    @EventListener
    public void onPluginIntegrationCreateWidgetEvent(AppPluginIntegrationCreateWidgetEvent event) {
        Widget widget = this.plugins.createWidget(event.getNamespace(), UUID.randomUUID().toString(), event.getName(), null);

        this.save();
        CaffeinatedApp.getInstance().getUI().navigate("/pages/edit-widget?widget=" + widget.getId());
    }

    @SneakyThrows
    @EventListener
    public void onPluginIntegrationRenameWidgetEvent(AppPluginIntegrationRenameWidgetEvent event) {
        Widget widget = this.plugins.getWidget(event.getId());

        ReflectionLib.setValue(widget, "name", event.getNewName());
        this.save();

        widget.onNameUpdate();
    }

    @EventListener
    public void onPluginIntegrationDeleteWidgetEvent(AppPluginIntegrationDeleteWidgetEvent event) {
        this.plugins.destroyWidget(event.getId());

        this.save();
    }

    @EventListener
    public void onPluginIntegrationEditWidgetSettingsEvent(AppPluginIntegrationEditWidgetSettingsEvent event) {
        Widget widget = this.plugins.getWidget(event.getId());

        try {
            JsonObject settings = ReflectionLib.getValue(widget, "settings");
            JsonElement value = event.getNewValue();

            // JsonNull should always be converted to null.
            if ((value == null) || value.isJsonNull()) {
                settings.remove(event.getKey());
            } else {
                settings.put(event.getKey(), value);
            }

            ReflectionLib.invokeMethod(widget, "$onSettingsUpdate", settings);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        this.save();
    }

    @EventListener
    public void onPluginIntegrationClickSettingsButtonEvent(AppPluginIntegrationClickWidgetSettingsButtonEvent event) {
        Widget widget = this.plugins.getWidget(event.getId());

        try {
            WidgetSettingsButton button = null;

            for (WidgetSettingsButton b : widget.getSettingsLayout().getButtons()) {
                if (b.getId().equals(event.getButtonId())) {
                    button = b;
                    break;
                }
            }

            if (button != null) {
                new AsyncTask(button.getOnClick());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @EventListener
    public void onPluginIntegrationCopyWidgetUrlEvent(AppPluginIntegrationCopyWidgetUrlEvent event) {
        Widget widget = this.plugins.getWidget(event.getId());

        String conductorKey = CaffeinatedApp.getInstance().getAppPreferences().get().getConductorKey();
        String url = String.format(
            "https://widgets.casterlabs.co/caffeinated/widget.html?pluginId=%s&widgetId=%s&authorization=%s",
            widget.getPlugin().getId(),
            widget.getId(),
            conductorKey
        );

        ClipboardUtil.copy(url);

        CaffeinatedApp.getInstance().getUI().showToast("Copied link to clipboard", UIBackgroundColor.PRIMARY);
    }

    public void updateBridgeData() {
        this.bridge.get().loadedPlugins = this.plugins.getPlugins();
        this.bridge.get().creatableWidgets = this.plugins.getCreatableWidgets();
        this.bridge.get().widgets = this.plugins.getWidgets();
        this.bridge.update();
    }

    public static void invokeEvent(JsonObject data, String nestedType) throws InvocationTargetException, JsonParseException {
        handler.call(
            Rson.DEFAULT.fromJson(
                data,
                AppPluginIntegrationEventType
                    .valueOf(nestedType)
                    .getEventClass()
            )
        );
    }

}
