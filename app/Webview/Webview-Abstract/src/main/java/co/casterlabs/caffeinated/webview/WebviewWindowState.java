package co.casterlabs.caffeinated.webview;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import co.casterlabs.caffeinated.webview.bridge.BridgeValue;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;

@Data
@JsonClass(exposeAll = true)
public class WebviewWindowState {
    private String icon;
    private boolean maximized;
    private boolean hasFocus;

    private int x;
    private int y;
    private int width = 800;
    private int height = 600;

    private BridgeValue<WebviewWindowState> bridge = new BridgeValue<>("window", this);

    /* Listeners to be utilized by the webview impl itself */

    @Setter
    @NonNull
    @Deprecated
    private Runnable onIconUpdate = WebviewWindowState::noop;

    public WebviewWindowState() {
        if (System.getProperty("awt.supported").equals("true")) {
            // Setup Defaults
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

            int monitorWidth = gd.getDisplayMode().getWidth();
            int monitorHeight = gd.getDisplayMode().getHeight();

            this.x = (monitorWidth - this.width) / 2;
            this.y = (monitorHeight - this.height) / 2;
        }
    }

    public void setIcon(@NonNull String newIcon) {
        this.icon = newIcon;
        this.onIconUpdate.run();
    }

    public void update() {
        this.bridge.update();
    }

    /* Override as needed */

    public int getMinWidth() {
        return 800;
    }

    public int getMinHeight() {
        return 580;
    }

    private static void noop() {}

}
