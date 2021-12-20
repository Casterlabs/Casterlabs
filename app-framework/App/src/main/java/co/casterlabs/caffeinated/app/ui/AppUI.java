package co.casterlabs.caffeinated.app.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import co.casterlabs.caffeinated.app.AppPreferences;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.bridge.AppBridge;
import co.casterlabs.caffeinated.app.bridge.BridgeValue;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.theming.ThemeManager;
import co.casterlabs.caffeinated.app.ui.UIPreferences.ChatViewerPreferences;
import co.casterlabs.caffeinated.app.ui.events.AppUIAppearanceUpdateEvent;
import co.casterlabs.caffeinated.app.ui.events.AppUIEventType;
import co.casterlabs.caffeinated.app.ui.events.AppUIOpenLinkEvent;
import co.casterlabs.caffeinated.app.ui.events.AppUISaveChatViewerPreferencesEvent;
import co.casterlabs.caffeinated.app.ui.events.AppUIThemeLoadedEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;

public class AppUI {
    private static final long TOAST_DURATION = 2250; // 2.25s

    private static EventHandler<AppUIEventType> handler = new EventHandler<>();
    private static BridgeValue<ChatViewerPreferences> bridge_ChatViewerPreferences = new BridgeValue<>("ui:chatViewerPreferences");

    private @Getter boolean uiFinishedLoad = false;

    public AppUI() {
        handler.register(this);
    }

    public void init() {
        bridge_ChatViewerPreferences.set(CaffeinatedApp.getInstance().getUiPreferences().get().getChatViewerPreferences());
    }

    @EventListener
    public void onUISaveChatViewerPreferencesEvent(AppUISaveChatViewerPreferencesEvent event) {
        ChatViewerPreferences preferences = event.getPreferences();

        bridge_ChatViewerPreferences.set(preferences);
        CaffeinatedApp.getInstance().getUiPreferences().get().setChatViewerPreferences(preferences);
        CaffeinatedApp.getInstance().getUiPreferences().save();
    }

    @EventListener
    public void onUIAppearanceUpdateEvent(AppUIAppearanceUpdateEvent event) {
        UIPreferences uiPrefs = CaffeinatedApp.getInstance().getUiPreferences().get();

        uiPrefs.setIcon(event.getIcon());
        uiPrefs.setTheme(event.getTheme());
        uiPrefs.setCloseToTray(event.isCloseToTray());
        uiPrefs.setMinimizeToTray(event.isMinimizeToTray());
        CaffeinatedApp.getInstance().getUiPreferences().save();

        ThemeManager.setTheme(event.getTheme(), "co.casterlabs.dark");
    }

    @EventListener
    public void onUIThemeLoadedEvent(AppUIThemeLoadedEvent event) {
        this.uiFinishedLoad = true;

        PreferenceFile<AppPreferences> prefs = CaffeinatedApp.getInstance().getAppPreferences();

        if (prefs.get().isNew()) {
            prefs.get().setNew(false);
            prefs.save();

            this.navigate("/welcome/step1");
        } else {
            AppAuth auth = CaffeinatedApp.getInstance().getAuth();
            if (!auth.isSignedIn()) {
                this.navigate("/signin");
            } else if (auth.isAuthorized()) {
                this.navigate("/home");
            } // Otherwise AppAuth will automagically move us there :D
        }
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

    public void showToast(@NonNull String message, @NonNull UIBackgroundColor background) {
        if (this.uiFinishedLoad) {
            String line = String.format(
                "Toastify(%s).showToast();",

                // Build the toastify options.
                new JsonObject()
                    .put("text", message)
                    .put("duration", TOAST_DURATION)
                    .put("close", true)
                    .put(
                        "style", new JsonObject()
                            .put("background", background.getColor())
                    )
            );

            AppBridge.eval(line);
        }
    }

    public void goBack() {
        if (this.uiFinishedLoad) {
            AppBridge.eval("history.back()");
        }
    }

    public void navigate(String path) {
        if (this.uiFinishedLoad) {
            AppBridge.emit("goto", JsonObject.singleton("path", path));
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
