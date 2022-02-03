package co.casterlabs.caffeinated.app.theming;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.webview.bridge.BridgeValue;
import co.casterlabs.caffeinated.window.theming.Theme;
import co.casterlabs.caffeinated.window.theming.ThemeableJFrame;
import lombok.Getter;
import lombok.NonNull;

public class ThemeManager {

    private static Map<String, Theme> themes = new HashMap<>();
    private static @Getter Theme currentTheme;

    private static BridgeValue<Theme> bridge_Theme = new BridgeValue<>("theme");
    private static BridgeValue<Collection<Theme>> bridge_Themes = new BridgeValue<>("themes");

    public static void init() {
        CaffeinatedApp.getInstance().getAppBridge().attachBridge(bridge_Theme);
        CaffeinatedApp.getInstance().getAppBridge().attachBridge(bridge_Themes);
    }

    public static void registerTheme(@NonNull Theme theme) {
        themes.put(theme.getId(), theme);
        bridge_Themes.set(themes.values());
    }

    public static void setTheme(@NonNull String id, @NonNull String defaultTheme) {
        Theme theme = themes.get(id);

        if (theme == null) {
            theme = themes.get(defaultTheme);
        }

        assert theme != null : "There is no theme registered with an id of '" + id + "' or '" + defaultTheme + "'";

        currentTheme = theme;
        bridge_Theme.set(theme);

        ThemeableJFrame.setDarkMode(theme.isDark());
    }

}
