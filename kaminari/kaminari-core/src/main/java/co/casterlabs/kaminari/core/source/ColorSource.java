package co.casterlabs.kaminari.core.source;

import co.casterlabs.kaminari.core.scene.Scene;
import lombok.NonNull;
import xyz.e3ndr.javawebcolor.Color;

public class ColorSource extends Source {

    public ColorSource(@NonNull Scene scene, @NonNull String id, @NonNull String name) {
        super(scene, id, name);

        this.panel.setOpaque(true); // We need the background to be opaque.
    }

    public void setColor(String color) {
        this.panel.setBackground(
            Color
                .parseCSSColor(color)
                .toAWTColor()
        );
    }

}
