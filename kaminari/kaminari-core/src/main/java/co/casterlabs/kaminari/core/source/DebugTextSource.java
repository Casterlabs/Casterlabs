package co.casterlabs.kaminari.core.source;

import java.awt.Font;

import co.casterlabs.kaminari.core.Kaminari;
import co.casterlabs.kaminari.core.scene.Scene;
import lombok.NonNull;
import xyz.e3ndr.javawebcolor.Color;

@Deprecated
public class DebugTextSource extends TextSource {

    public DebugTextSource(@NonNull Scene scene) {
        super(scene, "DEBUG-SOURCE", "Debug Source");

        // Styling.
        this.setTextColor("white");
        this.setFont(Font.MONOSPACED, 16);
        this.panel.setBackground(Color.parseCSSColor("rgba(0, 0, 0, .2)").toAWTColor());

        // Span the whole area.
        this.setPosition(0, 0);
        this.setSize(1, 1);
    }

    @Override
    public void onRender() {
        Kaminari kaminari = this.scene.getKaminari();

        String[] lines = {
        // @formatter:off
        String.format("Rendered frames: %d/%d",         kaminari.getFramesRendered(), kaminari.getFramesTargeted()),
        String.format("Frame time:      %dms",          kaminari.getFrameTime()),
        String.format("Video:           %dx%d @ %dfps", kaminari.getWidth(), kaminari.getHeight(), kaminari.getFrameRate())
        // @formatter:on
        };

        this.setHtml(
            String
                .join("<br />", lines)
                .replace(" ", "&nbsp;")
        );
    }

}
