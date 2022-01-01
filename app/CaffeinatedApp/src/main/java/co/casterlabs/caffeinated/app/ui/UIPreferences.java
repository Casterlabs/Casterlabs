package co.casterlabs.caffeinated.app.ui;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class UIPreferences {
    private String icon = "casterlabs";
    private String theme = "co.casterlabs.dark";
    private boolean closeToTray = true;
    private boolean minimizeToTray = false;
    private ChatViewerPreferences chatViewerPreferences = new ChatViewerPreferences();

    public String getIcon() {
        if (CaffeinatedApp.getInstance().isDev()) {
            return "hardhat";
        } else {
            return this.icon;
        }
    }

    @Data
    @JsonClass(exposeAll = true)
    public static class ChatViewerPreferences {
        private boolean showChatTimestamps = true;
        private boolean showModActions = true;
        private boolean showProfilePictures = false;
        private boolean showBadges = false;
        private boolean showViewers = false;
        private boolean showViewersList = true;

    }

}
