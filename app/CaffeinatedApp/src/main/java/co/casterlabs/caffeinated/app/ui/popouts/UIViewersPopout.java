package co.casterlabs.caffeinated.app.ui.popouts;

import co.casterlabs.caffeinated.app.preferences.PreferenceFile;

public class UIViewersPopout extends UIPopout {
    private static PreferenceFile<DockWindowState> state = new PreferenceFile<>("dock/internal/viewers-popout", DockWindowState.class);

    public UIViewersPopout() {
        super(state);
    }

    @Override
    protected String getUrl() {
        return "/popout/stream-viewers";
    }

}
