package co.casterlabs.caffeinated.app.preferences;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class WindowPreferences {
    private int x;
    private int y;
    private int width = 800;
    private int height = 600;
    private int stateFlags = JFrame.NORMAL;

    public WindowPreferences() {
        // Setup Defaults
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        int monitorWidth = gd.getDisplayMode().getWidth();
        int monitorHeight = gd.getDisplayMode().getHeight();

        this.x = (monitorWidth - this.width) / 2;
        this.y = (monitorHeight - this.height) / 2;
    }

}
