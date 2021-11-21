package co.casterlabs.caffeinated.app.ui;

import java.lang.reflect.InvocationTargetException;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.ui.events.AppUIAppearanceUpdateEvent;
import co.casterlabs.caffeinated.app.ui.events.AppUIEventType;
import co.casterlabs.caffeinated.app.ui.events.AppUIThemeLoadedEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;

public class AppearanceManager {
    private static EventHandler<AppUIEventType> handler = new EventHandler<>();

    public AppearanceManager() {
        handler.register(this);
    }

    @EventListener
    public void onUIAppearanceUpdateEvent(AppUIAppearanceUpdateEvent event) {
        UIPreferences uiPrefs = CaffeinatedApp.getInstance().getUiPreferences().get();

        uiPrefs.setIcon(event.getIcon());
        uiPrefs.setTheme(event.getTheme());
        CaffeinatedApp.getInstance().getUiPreferences().save();
    }

    @EventListener
    public void onUIThemeLoadedEvent(AppUIThemeLoadedEvent event) {
        CaffeinatedApp.getInstance().getBridge().emit("goto", JsonObject.singleton("path", "/welcome/step1"));
    }

    public static void invokeEvent(JsonObject data, String nestedType) throws InvocationTargetException, JsonParseException {
        handler.call(
            Rson.DEFAULT.fromJson(
                data,
                AppUIEventType
                    .valueOf(nestedType)
                    .getEventClass()
            )
        );
    }

}
