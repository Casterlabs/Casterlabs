package co.casterlabs.caffeinated.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.auth.AuthPreferences;
import co.casterlabs.caffeinated.app.koi.GlobalKoi;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegrationPreferences;
import co.casterlabs.caffeinated.app.plugins.PluginIntegration;
import co.casterlabs.caffeinated.app.plugins.PluginIntegrationPreferences;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.preferences.WindowPreferences;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.app.ui.UIPreferences;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;

@Getter
public class CaffeinatedApp {
    public static final String caffeinatedClientId = "LmHG2ux992BxqQ7w9RJrfhkW";

    // I chose JsonObject because of the builder syntax.
    public static final JsonObject AUTH_URLS = new JsonObject()
        .put("caffeinated_spotify", "https://accounts.spotify.com/en/authorize?client_id=dff9da1136b0453983ff40e3e5e20397&redirect_uri=https:%2F%2Fcasterlabs.co%2Fauth%3Ftype%3Dcaffeinated_spotify&response_type=code&scope=user-read-playback-state&state=")
        .put("caffeinated_twitch", "https://id.twitch.tv/oauth2/authorize?client_id=ekv4a842grsldmwrmsuhrw8an1duxt&force_verify=true&redirect_uri=https%3A%2F%2Fcasterlabs.co%2Fauth&response_type=code&scope=user:read:email%20chat:read%20chat:edit%20bits:read%20channel:read:subscriptions%20channel_subscriptions%20channel:read:redemptions&state=")
        .put("caffeinated_trovo", "https://open.trovo.live/page/login.html?client_id=BGUnwUJUSJS2wf5xJpa2QrJRU4ZVcMgS&redirect_uri=https%3A%2F%2Fcasterlabs.co%2Fauth%2Ftrovo&response_type=token&scope=channel_details_self+chat_send_self+send_to_my_channel+user_details_self+chat_connect&state=")
        .put("caffeinated_glimesh", "https://glimesh.tv/oauth/authorize?client_id=3c60c5b45bbae0eadfeeb35d1ee0c77e580b31fd42a5fbc8ae965ca7106c5139&force_verify=true&redirect_uri=https%3A%2F%2Fcasterlabs.co%2Fauth%2Fglimesh&response_type=code&scope=public+email+chat&state=")
        .put("caffeinated_brime", "https://auth.brime.tv/authorize?client_id=l87k8wMUeyuotnCp9HFsOzQ4gTi66atj&redirect_uri=https%3A%2F%2Fcasterlabs.co%2Fauth&response_type=code&scope=offline_access&state=");

    private static @Getter CaffeinatedApp instance;

    private final BuildInfo buildInfo;
    private final boolean isDev;
    private @Setter AppBridge bridge;

    private AppAuth auth = new AppAuth();
    private MusicIntegration musicIntegration = new MusicIntegration();
    private GlobalKoi koi = new GlobalKoi();
    private AppUI UI = new AppUI();
    private PluginIntegration plugins = new PluginIntegration();

    private PreferenceFile<WindowPreferences> windowPreferences = new PreferenceFile<>("window", WindowPreferences.class);
    private PreferenceFile<UIPreferences> uiPreferences = new PreferenceFile<>("ui", UIPreferences.class);
    private PreferenceFile<AuthPreferences> authPreferences = new PreferenceFile<>("auth", AuthPreferences.class);
    private PreferenceFile<MusicIntegrationPreferences> musicIntegrationPreferences = new PreferenceFile<>("music", MusicIntegrationPreferences.class);
    private PreferenceFile<PluginIntegrationPreferences> pluginIntegrationPreferences = new PreferenceFile<>("plugins", PluginIntegrationPreferences.class);

    private Map<String, List<Consumer<JsonObject>>> bridgeEventListeners = new HashMap<>();
    private Map<String, List<Consumer<JsonObject>>> appEventListeners = new HashMap<>();

    public CaffeinatedApp(@NonNull BuildInfo buildInfo, boolean isDev) {
        this.buildInfo = buildInfo;
        this.isDev = isDev;
        instance = this;
    }

    public void init() {
        this.uiPreferences.addSaveListener(this::saveListener);

        this.auth.init();
        this.musicIntegration.init();
        this.plugins.init();
    }

    public boolean canCloseUI() {
        // We can prevent ui closure if needed.
        // Maybe during plugin installs?
        // TODO
        return true;
    }

    public void shutdown() {
        this.auth.shutdown();
    }

    /**
     * Word of caution, you're not supposed to be able to unsubscribe to an event.
     * You have been warned.
     * 
     * If u throw err, i kil.
     */
    public void onBridgeEvent(@NonNull String type, @NonNull Consumer<JsonObject> handler) {
        if (!this.bridgeEventListeners.containsKey(type)) {
            this.bridgeEventListeners.put(type, new LinkedList<>());
        }

        this.bridgeEventListeners.get(type).add(handler);
    }

    /**
     * Word of caution, you're not supposed to be able to unsubscribe to an event.
     * You have been warned.
     * 
     * If u throw err, i kil.
     */
    public void onAppEvent(@NonNull String type, @NonNull Consumer<JsonObject> handler) {
        if (!this.appEventListeners.containsKey(type)) {
            this.appEventListeners.put(type, new LinkedList<>());
        }

        this.appEventListeners.get(type).add(handler);
    }

    public void emitAppEvent(@NonNull String type, @NonNull JsonObject data) {
        if (this.appEventListeners.containsKey(type)) {
            this.appEventListeners
                .get(type)
                .forEach((c) -> c.accept(data));
        }
    }

    @SneakyThrows
    public void onBridgeEvent(String type, JsonObject data) {
        String[] signal = type.split(":", 2);
        String nestedType = signal[1].replace('-', '_').toUpperCase();

        if (this.bridgeEventListeners.containsKey(type)) {
            this.bridgeEventListeners
                .get(type)
                .forEach((c) -> c.accept(data));
        }

        switch (signal[0].toLowerCase()) {

            case "ui": {
                AppUI.invokeEvent(data, nestedType);
                return;
            }

            case "auth": {
                AppAuth.invokeEvent(data, nestedType);
                return;
            }

            case "music": {
                MusicIntegration.invokeEvent(data, nestedType);
                return;
            }

            case "plugin": {
                PluginIntegration.invokeEvent(data, nestedType);
                return;
            }

        }
    }

    // This pretty much emits & updates the query data every time a
    // preference is saved.
    public void saveListener(PreferenceFile<?> pref) {
        JsonElement json = Rson.DEFAULT.toJson(pref.get());

        this.bridge.getQueryData().put(pref.getName(), json);
        this.bridge.emit("pref-update:" + pref.getName(), json);
    }

}
