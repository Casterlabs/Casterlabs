package co.casterlabs.kaminari.core.source;

import co.casterlabs.kaminari.core.Kaminari;
import lombok.NonNull;
import xyz.e3ndr.javawebcolor.Color;

public class ColorSource extends Source {

    public ColorSource(@NonNull Kaminari kaminari, @NonNull String id, @NonNull String name) {
        super(kaminari, id, name);

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
