package co.casterlabs.caffeinated.app;

import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.preferences.WindowPreferences;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class CaffeinatedApp {
    private final BuildInfo buildInfo;
    private final boolean isDev;
    private @Setter AppBridge bridge;

    private PreferenceFile<WindowPreferences> windowPreferences = new PreferenceFile<>("window", WindowPreferences.class);

    public void init() {

    }

    public void onBridgeEvent(String type, JsonObject data) {

    }

}
