package co.casterlabs.kaminari.core.source;

import java.awt.Font;
import java.awt.Graphics;

import co.casterlabs.kaminari.core.Kaminari;
import co.casterlabs.kaminari.core.Looper;
import co.casterlabs.kaminari.core.scene.Scene;
import lombok.NonNull;
import xyz.e3ndr.javawebcolor.Color;

@Deprecated
public class DebugTextSource extends TextSource {
    private java.awt.Color backgroundColor;

    public DebugTextSource(@NonNull Scene scene) {
        super(scene, "DEBUG-SOURCE", "Debug Source");

        // Styling.
        this.setTextColor("white");
        this.setFont(Font.MONOSPACED, 16);
        this.backgroundColor = (Color.parseCSSColor("rgba(0, 0, 0, .2)").toAWTColor());

        // Span the whole area.
        this.setPosition(0, 0);
        this.setSize(1, 1);
    }

    @Override
    public void render(Graphics g) {
        Kaminari kaminari = this.scene.getKaminari();
        Looper videoLooper = kaminari.getVideoLooper();

        this.setLines(
        // @formatter:off
            String.format("Rendered frames: %d/%d",         videoLooper.getFramesRendered(), videoLooper.getFramesTargeted()),
            String.format("Frame time:      %dms",          videoLooper.getFrameTime()),
            String.format("Video:           %dx%d @ %dfps", kaminari.getWidth(), kaminari.getHeight(), kaminari.getFrameRate())
        // @formatter:on
        );

//        g.setColor(this.backgroundColor);
//        g.fillRect(0, 0, this.bounds.width, this.bounds.height);

        super.render(g);
    }

}
