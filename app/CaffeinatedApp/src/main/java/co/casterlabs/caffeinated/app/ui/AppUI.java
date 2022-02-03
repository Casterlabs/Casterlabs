package co.casterlabs.caffeinated.app.ui;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.casterlabs.caffeinated.app.AppPreferences;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.theming.ThemeManager;
import co.casterlabs.caffeinated.app.ui.UIPreferences.ChatViewerPreferences;
import co.casterlabs.caffeinated.app.ui.events.AppUIAppearanceUpdateEvent;
import co.casterlabs.caffeinated.app.ui.events.AppUIEventType;
import co.casterlabs.caffeinated.app.ui.events.AppUIOpenLinkEvent;
import co.casterlabs.caffeinated.app.ui.events.AppUISaveChatViewerPreferencesEvent;
import co.casterlabs.caffeinated.app.ui.events.AppUIThemeLoadedEvent;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.webview.Webview;
import co.casterlabs.caffeinated.webview.bridge.BridgeValue;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.Request;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AppUI {
    private static final String GOOGLE_FONTS_API_KEY = "AIzaSyBuFeOYplWvsOlgbPeW8OfPUejzzzTCITM";

    private static final long TOAST_DURATION = 2250; // 2.25s

    private static EventHandler<AppUIEventType> handler = new EventHandler<>();
    private static BridgeValue<ChatViewerPreferences> bridge_ChatViewerPreferences = new BridgeValue<>("ui:chatViewerPreferences");

    private @Getter boolean uiFinishedLoad = false;

    private @Getter List<String> systemFonts = Collections.emptyList();
    private @Getter List<String> googleFonts = Collections.emptyList();
    private @Getter List<String> allFonts = Collections.emptyList();

    public AppUI() {
        handler.register(this);

        new AsyncTask(() -> {
            try {
                GraphicsEnvironment ge = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();

                Font[] allFonts = ge.getAllFonts();
                Set<String> systemFonts = new HashSet<>(allFonts.length);

                for (Font font : allFonts) {
                    systemFonts.add(font.getFamily().trim());
                }

                List<String> listOfSystemFonts = new ArrayList<>(systemFonts);

                Collections.sort(listOfSystemFonts);

                this.systemFonts = Collections.unmodifiableList(listOfSystemFonts);
            } catch (Exception e) {
                FastLogger.logException(e);
            }

            try {
                JsonObject response = Rson.DEFAULT.fromJson(
                    WebUtil.sendHttpRequest(new Request.Builder().url("https://www.googleapis.com/webfonts/v1/webfonts?sort=popularity&key=" + GOOGLE_FONTS_API_KEY)),
                    JsonObject.class
                );

                if (response.containsKey("items")) {
                    JsonArray items = response.getArray("items");
                    List<String> googleFonts = new ArrayList<>(items.size());

                    for (JsonElement e : items) {
                        JsonObject font = e.getAsObject();

                        googleFonts.add(font.getString("family").trim());
                    }

                    Collections.sort(googleFonts);

                    this.googleFonts = Collections.unmodifiableList(googleFonts);
                }
            } catch (Exception e) {
                FastLogger.logException(e);
            }

            Set<String> combined = new HashSet<>(this.systemFonts.size() + this.googleFonts.size());

            combined.addAll(this.systemFonts);
            combined.addAll(this.googleFonts);

            List<String> allFonts = new ArrayList<>(combined);

            Collections.sort(allFonts);

            this.allFonts = Collections.unmodifiableList(allFonts);
        });
    }

    public void init() {
        CaffeinatedApp.getInstance().getAppBridge().attachBridge(bridge_ChatViewerPreferences);

        CaffeinatedApp.getInstance().getAppBridge().attachBridge(
            // This doesn't update, so we register it and leave it be.
            new BridgeValue<List<String>>("ui:fonts").set(this.allFonts)
        );

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

        Webview.getWebviewFactory().setIcon(uiPrefs.getIcon());

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
            } else {
                // Otherwise AppAuth will automagically move us there :D
                FastLogger.logStatic(LogLevel.DEBUG, "Waiting for auth to navigate us. (theme-loaded)");
            }
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

            CaffeinatedApp.getInstance().getAppBridge().eval(line);
        }
    }

    public void goBack() {
        if (this.uiFinishedLoad) {
            CaffeinatedApp.getInstance().getAppBridge().eval("history.back()");
        }
    }

    public void navigate(String path) {
        if (this.uiFinishedLoad) {
            CaffeinatedApp.getInstance().getAppBridge().emit("goto", JsonObject.singleton("path", path));
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
