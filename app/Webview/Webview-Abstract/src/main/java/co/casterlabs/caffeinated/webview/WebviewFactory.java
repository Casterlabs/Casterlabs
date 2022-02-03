package co.casterlabs.caffeinated.webview;

import co.casterlabs.caffeinated.util.Producer;
import lombok.Getter;
import lombok.NonNull;

public abstract class WebviewFactory implements Producer<Webview> {
    private static @Getter String currentIcon = "casterlabs";

    public void setIcon(@NonNull String icon) {
        currentIcon = icon;
        this.setIcon0(icon);
    }

    public abstract boolean useNuclearOption();

    protected abstract void setIcon0(@NonNull String icon);

}
