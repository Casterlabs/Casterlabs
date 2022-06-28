package co.casterlabs.kaminari.core.source;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import co.casterlabs.kaminari.core.scene.Scene;
import lombok.NonNull;
import xyz.e3ndr.javawebcolor.Color;

public class TextSource extends Source {
    protected JLabel label = new JLabel();

    public TextSource(@NonNull Scene scene, @NonNull String id, @NonNull String name) {
        super(scene, id, name);

        this.panel.setLayout(new BorderLayout());
        this.panel.add(this.label);
        this.label.setSize(100000, 100000); // Any large number works.

        this.setTextColor("white");
        this.setFont("Arial", 12);

        this.label.setVerticalAlignment(SwingConstants.TOP);
    }

    public void setFont(String font, int size) {
        this.label.setFont(new Font(font, Font.PLAIN, size));
    }

    public void setTextColor(String color) {
        this.label.setForeground(
            Color
                .parseCSSColor(color)
                .toAWTColor()
        );
    }

    public void setText(String text) {
        this.setHtml(
            escapeHtml(text)
                .replace(" ", "&nbsp;")
                .replace("\n", "<br />")
        );
    }

    public void setHtml(String html) {
        this.label.setText(
            "<html>" +
                html +
                "</html>"
        );
    }

    private static String escapeHtml(@NonNull String str) {
        return str
            .codePoints()
            .mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ? "&#" + c + ";" : new String(Character.toChars(c)))
            .collect(Collectors.joining());
    }

}
