package co.casterlabs.caffeinated.app.plugins;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import co.casterlabs.caffeinated.app.AppBridge;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.plugins.PluginIntegrationPreferences.WidgetSettingsDetails;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationCreateWidgetEvent;
import co.casterlabs.caffeinated.app.plugins.events.AppPluginIntegrationEventType;
import co.casterlabs.caffeinated.app.plugins.impl.PluginsHandler;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.pluginsdk.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;

@Getter
public class PluginIntegration {
    private static EventHandler<AppPluginIntegrationEventType> handler = new EventHandler<>();

    private PluginsHandler plugins = new PluginsHandler();

    public void init() {
        // Load the built-in widgets.
        this.plugins.loadPluginsFromClassLoader(this.getClass().getClassLoader());

        // TODO load external jars

        for (Map.Entry<String, WidgetSettingsDetails> entry : CaffeinatedApp.getInstance().getPluginIntegrationPreferences().get().getWidgetSettings().entrySet()) {
            String id = entry.getKey();
            WidgetSettingsDetails details = entry.getValue();

            try {
                // Reconstruct the widget.
                this.plugins.createWidget(details.getNamespace(), id, details.getName());
            } catch (AssertionError e) {
                if (e.getMessage().equals("A factory associated to that widget is not registered.")) {
                    // We can safely ignore it.
                    // TODO let the user know that the widget could not be found.
                } else {
                    throw e;
                }
            }
        }

        this.save();
    }

    public void save() {
        PreferenceFile<PluginIntegrationPreferences> prefs = CaffeinatedApp.getInstance().getPluginIntegrationPreferences();

        Map<String, WidgetSettingsDetails> widgetSettings = new HashMap<>();
        for (Widget widget : this.plugins.getWidgets()) {
            widgetSettings.put(widget.getId(), WidgetSettingsDetails.from(widget));
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

    @SuppressWarnings("deprecation")
    public void updateBridgeData() {
        JsonObject widgets = new JsonObject();
        for (Widget widget : this.plugins.getWidgets()) {
            widgets.put(widget.getId(), widget.toJson());
        }

        JsonArray createableWidgets = new JsonArray();
        for (WidgetDetails details : this.plugins.getCreatableWidgets()) {
            createableWidgets.add(Rson.DEFAULT.toJson(details));
        }

        JsonObject bridgeData = new JsonObject()
            .put("widgets", widgets)
            .put("createableWidgets", createableWidgets);

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
