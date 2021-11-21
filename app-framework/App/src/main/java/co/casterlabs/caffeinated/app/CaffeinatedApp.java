package co.casterlabs.caffeinated.app;

import co.casterlabs.caffeinated.app.auth.AppAuth;
import co.casterlabs.caffeinated.app.auth.AuthPreferences;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.preferences.WindowPreferences;
import co.casterlabs.caffeinated.app.ui.AppearanceManager;
import co.casterlabs.caffeinated.app.ui.UIPreferences;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

@Getter
@RequiredArgsConstructor
public class CaffeinatedApp {
    public static final String caffeinatedClientId = "LmHG2ux992BxqQ7w9RJrfhkW";

    // I chose JsonObject because of the builder syntax.
    public static final JsonObject AUTH_URLS = new JsonObject()
//        .put("caffeinated_spotify", "https://accounts.spotify.com/en/authorize?client_id=dff9da1136b0453983ff40e3e5e20397&redirect_uri=https:%2F%2Fcasterlabs.co%2Fauth%3Ftype%3Dcaffeinated_spotify&response_type=code&scope=user-read-playback-state&state=")
        .put("caffeinated_twitch", "https://id.twitch.tv/oauth2/authorize?client_id=ekv4a842grsldmwrmsuhrw8an1duxt&force_verify=true&redirect_uri=https%3A%2F%2Fcasterlabs.co%2Fauth&response_type=code&scope=user:read:email%20chat:read%20chat:edit%20bits:read%20channel:read:subscriptions%20channel_subscriptions%20channel:read:redemptions&state=")
        .put("caffeinated_trovo", "https://open.trovo.live/page/login.html?client_id=BGUnwUJUSJS2wf5xJpa2QrJRU4ZVcMgS&redirect_uri=https%3A%2F%2Fcasterlabs.co%2Fauth%2Ftrovo&response_type=token&scope=channel_details_self+chat_send_self+send_to_my_channel+user_details_self+chat_connect&state=")
        .put("caffeinated_glimesh", "https://glimesh.tv/oauth/authorize?client_id=3c60c5b45bbae0eadfeeb35d1ee0c77e580b31fd42a5fbc8ae965ca7106c5139&force_verify=true&redirect_uri=https%3A%2F%2Fcasterlabs.co%2Fauth%2Fglimesh&response_type=code&scope=public+email+chat&state=")
        .put("caffeinated_brime", "https://auth.brime.tv/authorize?client_id=l87k8wMUeyuotnCp9HFsOzQ4gTi66atj&redirect_uri=https%3A%2F%2Fcasterlabs.co%2Fauth&response_type=code&scope=offline_access&state=");

    private static @Getter CaffeinatedApp instance;

    private final BuildInfo buildInfo;
    private final boolean isDev;
    private @Setter AppBridge bridge;

    private AppAuth auth = new AppAuth();

    private PreferenceFile<WindowPreferences> windowPreferences = new PreferenceFile<>("window", WindowPreferences.class);
    private PreferenceFile<UIPreferences> uiPreferences = new PreferenceFile<>("ui", UIPreferences.class);
    private PreferenceFile<AuthPreferences> authPreferences = new PreferenceFile<>("auth", AuthPreferences.class);

    private AppearanceManager appearanceManager = new AppearanceManager();

    public void init() {
        instance = this;

        this.uiPreferences.addSaveListener(this::saveListener);

        auth.updateBridgeData();
    }

    @SneakyThrows
    public void onBridgeEvent(String type, JsonObject data) {
        String[] signal = type.split(":", 2);
        String nestedType = signal[1].replace('-', '_').toUpperCase();

        switch (signal[0].toLowerCase()) {

            case "ui": {
                AppearanceManager.invokeEvent(data, nestedType);
                return;
            }

            case "auth": {
                AppAuth.invokeEvent(data, nestedType);
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
