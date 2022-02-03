package co.casterlabs.caffeinated.webview;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import co.casterlabs.caffeinated.webview.bridge.BridgeValue;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class WebviewWindowState {
    private boolean maximized = false;
    private boolean hasFocus;

    private int x;
    private int y;
    private int width = 800;
    private int height = 600;

    private @JsonExclude BridgeValue<WebviewWindowState> bridge = new BridgeValue<>("window", this);

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

}
