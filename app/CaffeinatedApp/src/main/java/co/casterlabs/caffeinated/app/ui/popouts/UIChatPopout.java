package co.casterlabs.caffeinated.app.ui.popouts;

import co.casterlabs.caffeinated.app.preferences.PreferenceFile;

public class UIChatPopout extends UIPopout {
    private static PreferenceFile<DockWindowState> state = new PreferenceFile<>("dock/internal/chat-popout", DockWindowState.class);

    public UIChatPopout() {
        super(state);
    }

    @Override
    protected String getUrl() {
        return "/popout/stream-chat";
    }

}
