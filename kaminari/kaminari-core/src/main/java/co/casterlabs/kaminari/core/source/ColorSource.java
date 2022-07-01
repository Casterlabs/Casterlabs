package co.casterlabs.kaminari.core.source;

import java.awt.Graphics;

import co.casterlabs.kaminari.core.scene.Scene;
import lombok.NonNull;
import xyz.e3ndr.javawebcolor.Color;

public class ColorSource extends Source {
    private java.awt.Color awtColor;

    public ColorSource(@NonNull Scene scene, @NonNull String id, @NonNull String name) {
        super(scene, id, name);
    }

    public void setColor(String color) {
        this.awtColor = Color
            .parseCSSColor(color)
            .toAWTColor();
    }

    @Override
    public void render(Graphics g) {
        g.setColor(this.awtColor);
        g.fillRect(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);
    }

}
