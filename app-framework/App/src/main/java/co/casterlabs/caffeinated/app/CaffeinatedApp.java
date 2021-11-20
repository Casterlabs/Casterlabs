package co.casterlabs.caffeinated.app;

import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.preferences.WindowPreferences;
import co.casterlabs.caffeinated.app.ui.AppearanceManager;
import co.casterlabs.caffeinated.app.ui.UIPreferences;
import co.casterlabs.caffeinated.app.ui.events.AppUIEventType;
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
    private static @Getter CaffeinatedApp instance;

    private final BuildInfo buildInfo;
    private final boolean isDev;
    private @Setter AppBridge bridge;

    private PreferenceFile<WindowPreferences> windowPreferences = new PreferenceFile<>("window", WindowPreferences.class);
    private PreferenceFile<UIPreferences> uiPreferences = new PreferenceFile<>("ui", UIPreferences.class);

    private AppearanceManager appearanceManager = new AppearanceManager();

    public void init() {
        instance = this;

        this.uiPreferences.addSaveListener(this::saveListener);

    }

    @SneakyThrows
    public void onBridgeEvent(String type, JsonObject data) {
        String[] signal = type.split(":", 2);
        String nestedType = signal[1].replace('-', '_').toUpperCase();

        switch (signal[0].toLowerCase()) {
            case "ui": {
                this.appearanceManager.handler.call(
                    Rson.DEFAULT.fromJson(
                        data,
                        AppUIEventType
                            .valueOf(nestedType)
                            .getEventClass()
                    )
                );
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
