package co.casterlabs.caffeinated.app.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.ui.events.AppUIAppearanceUpdateEvent;
import co.casterlabs.caffeinated.app.ui.events.AppUIEventType;
import co.casterlabs.caffeinated.app.ui.events.AppUIOpenLinkEvent;
import co.casterlabs.caffeinated.app.ui.events.AppUIThemeLoadedEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;

public class AppUI {
    private static EventHandler<AppUIEventType> handler = new EventHandler<>();

    private @Getter boolean uiFinishedLoad = false;

    public AppUI() {
        handler.register(this);
    }

    @EventListener
    public void onUIAppearanceUpdateEvent(AppUIAppearanceUpdateEvent event) {
        UIPreferences uiPrefs = CaffeinatedApp.getInstance().getUiPreferences().get();

        uiPrefs.setIcon(event.getIcon());
        uiPrefs.setTheme(event.getTheme());
        uiPrefs.setCloseToTray(event.isCloseToTray());
        uiPrefs.setMinimizeToTray(event.isMinimizeToTray());
        CaffeinatedApp.getInstance().getUiPreferences().save();
    }

    @EventListener
    public void onUIThemeLoadedEvent(AppUIThemeLoadedEvent event) {
        this.uiFinishedLoad = true;

        AppAuth auth = CaffeinatedApp.getInstance().getAuth();
        if (!auth.isSignedIn()) {
            this.navigate("/signin");
        } else if (auth.isAuthorized()) {
            this.navigate("/home");
        } // Otherwise AppAuth will automagically move us there :D
    }

    @EventListener
    public void onUIOpenLinkEvent(AppUIOpenLinkEvent event) {
        try {
            Desktop
                .getDesktop()
                .browse(new URI(event.getLink()));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public void goBack() {
        if (this.uiFinishedLoad) {
            CaffeinatedApp
                .getInstance()
                .getBridge()
                .eval("history.back()");
        }
    }

    public void navigate(String path) {
        if (this.uiFinishedLoad) {
            CaffeinatedApp
                .getInstance()
                .getBridge()
                .emit("goto", JsonObject.singleton("path", path));
        }
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