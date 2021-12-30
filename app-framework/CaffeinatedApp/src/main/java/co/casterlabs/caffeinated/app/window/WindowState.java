package co.casterlabs.caffeinated.app.window;

import co.casterlabs.caffeinated.app.bridge.BridgeValue;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class WindowState {
    private String title;
    private String icon;
    private boolean maximized;
    private String platform;
    private boolean hasFocus;
    private boolean enableCustomTitleBar;

    @Deprecated
    @Getter(AccessLevel.NONE)
    public final Unsafe_WindowState unsafe = new Unsafe_WindowState();

    private BridgeValue<WindowState> bridge = new BridgeValue<>("window", this);

    public class Unsafe_WindowState {

        public void update() {
            bridge.update();
        }

        /* Setters */

        public void title(String value) {
            title = value;
        }

        public void icon(String value) {
            icon = value;
        }

        public void maximized(boolean value) {
            maximized = value;
        }

        public void platform(String value) {
            platform = value;
        }

        public void hasFocus(boolean value) {
            hasFocus = value;
        }

        public void enableCustomTitleBar(boolean value) {
            enableCustomTitleBar = value;
        }

    }

}
