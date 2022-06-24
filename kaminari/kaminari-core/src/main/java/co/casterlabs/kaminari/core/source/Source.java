package co.casterlabs.kaminari.core.source;

import javax.swing.JPanel;

import co.casterlabs.kaminari.core.Kaminari;
import lombok.Getter;
import lombok.NonNull;

public abstract class Source {
    public final String id;
    public final Kaminari kaminari;

    public String name;

    public final JPanel panel = new JPanel();

    private @Getter float width, height; // Percent
    private @Getter float x, y;          // Percent

    private @Getter boolean hasErrored = false;

    public Source(@NonNull Kaminari kaminari, @NonNull String id, @NonNull String name) {
        this.kaminari = kaminari;
        this.id = id;
        this.name = name;

        this.panel.setOpaque(false);
    }

    /* ---------------- */
    /* Properties       */
    /* ---------------- */

    public void setSize(float width, float height) {
        assert width > 0 : "Width must be greater than 0.";
        assert height > 0 : "Height must be greater than 0.";

        this.width = width;
        this.height = height;

        this.kaminari.pack(this);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;

        this.kaminari.pack(this);
    }

    /* ---------------- */
    /* Life Cycle       */
    /* ---------------- */

    public void onMount() throws Throwable {}

    public void onDestroy() {}

    public void onRender() {}

}
