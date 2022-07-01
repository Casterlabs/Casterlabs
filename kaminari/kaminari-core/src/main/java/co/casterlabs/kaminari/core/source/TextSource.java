package co.casterlabs.kaminari.core.source;

import java.awt.Font;
import java.awt.Graphics;

import co.casterlabs.kaminari.core.scene.Scene;
import lombok.NonNull;
import xyz.e3ndr.javawebcolor.Color;

public class TextSource extends Source {
    private java.awt.Color foregroundColor;
    private Font font;
    private String[] lines;

    public TextSource(@NonNull Scene scene, @NonNull String id, @NonNull String name) {
        super(scene, id, name);

        this.setTextColor("white");
        this.setFont("Arial", 12);
        this.setText("");
    }

    public void setFont(String font, int size) {
        this.font = new Font(font, Font.PLAIN, size);
    }

    public void setTextColor(String color) {
        this.foregroundColor = Color
            .parseCSSColor(color)
            .toAWTColor();
    }

    public void setText(String text) {
        this.setLines(text.split("\n"));
    }

    public void setLines(String... lines) {
        this.lines = lines;
    }

    @Override
    public void render(Graphics g) {
        g.setColor(this.foregroundColor);
        g.setFont(this.font);

        int charHeight = g.getFontMetrics().getHeight();

        for (int idx = 0; idx < this.lines.length; idx++) {
            int yCalc = this.bounds.y + (charHeight * (idx + 1));
            g.drawString(this.lines[idx], this.bounds.x, yCalc);
        }
    }

}
