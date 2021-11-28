package co.casterlabs.caffeinated.app.plugins;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import co.casterlabs.caffeinated.app.AppBridge;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.plugins.PluginIntegrationPreferences.WidgetSettingsDetails;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationCreateWidgetEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationDeleteWidgetEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationEventType;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationRenameWidgetEvent;
import co.casterlabs.caffeinated.app.plugins.impl.PluginsHandler;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
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

    private PluginsHandler plugins = new PluginsHandler();

    public PluginIntegration() {
        handler.register(this);
    }

    public void init() {
        // Load the built-in widgets.
        this.plugins.loadPluginsFromClassLoader(this.getClass().getClassLoader());

        // TODO load external jars

        for (WidgetSettingsDetails details : CaffeinatedApp.getInstance().getPluginIntegrationPreferences().get().getWidgetSettings()) {
            try {
                // Reconstruct the widget.
                this.plugins.createWidget(details.getNamespace(), details.getId(), details.getName());
            } catch (AssertionError e) {
                if (e.getMessage().equals("A factory associated to that widget is not registered.")) {
                    // We can safely ignore it.
                    // TODO let the user know that the widget could not be found.
                    FastLogger.logStatic(LogLevel.WARNING, "Unable to create missing widget: %s (%s)", details.getName(), details.getNamespace());
                } else {
                    throw e;
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
        Widget widget = this.plugins.createWidget(event.getNamespace(), UUID.randomUUID().toString(), event.getName());

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
    public void onPluginIntegrationDeleteCreateWidgetEvent(AppPluginIntegrationDeleteWidgetEvent event) {
        this.plugins.destroyWidget(event.getId());

        this.save();

        CaffeinatedApp.getInstance().getUI().goBack();
    }

    @SuppressWarnings("deprecation")
    public void updateBridgeData() {
        JsonObject widgets = new JsonObject();
        for (Widget widget : this.plugins.getWidgets()) {
            widgets.put(widget.getId(), widget.toJson());
        }

        JsonArray creatableWidgets = new JsonArray();
        for (WidgetDetails details : this.plugins.getCreatableWidgets()) {
            creatableWidgets.add(Rson.DEFAULT.toJson(details));
        }

        JsonArray loadedPlugins = new JsonArray();
        for (CaffeinatedPlugin plugin : this.plugins.getPlugins()) {
            loadedPlugins.add(plugin.toJson());
        }

        JsonObject bridgeData = new JsonObject()
            .put("widgets", widgets)
            .put("creatableWidgets", creatableWidgets)
            .put("loadedPlugins", loadedPlugins);

        AppBridge bridge = CaffeinatedApp.getInstance().getBridge();

        bridge.getQueryData().put("plugins", bridgeData);
        bridge.emit("plugins:update", bridgeData);
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
