package co.casterlabs.kaminari.core.source;

import co.casterlabs.kaminari.core.Kaminari;
import lombok.NonNull;
import xyz.e3ndr.javawebcolor.Color;
import xyz.e3ndr.javawebcolor.ColorException;

public class ColorSource extends Source {

    public ColorSource(@NonNull Kaminari kaminari, @NonNull String id, @NonNull String name) {
        super(kaminari, id, name);

        this.panel.setOpaque(true); // We need the background to be opaque.
    }

    @Override
    public void onMount() throws ColorException {
        // NO-OP
    }

    @Override
    public void onDestroy() {
        // NO-OP
    }

    public void setColor(String color) throws ColorException {
        this.panel.setBackground(
            Color
                .parseCSSColor(color)
                .toAWTColor()
        );
        this.panel.revalidate();
    }

}
