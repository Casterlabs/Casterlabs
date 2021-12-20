package co.casterlabs.caffeinated.app.ui;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class UIPreferences {
    private String icon = "casterlabs";
    private String theme = "co.casterlabs.dark";
    private boolean closeToTray = true;
    private boolean minimizeToTray = false;
    private ChatViewerPreferences viewerPreferences = new ChatViewerPreferences();

    @Data
    @JsonClass(exposeAll = true)
    public static class ChatViewerPreferences {

    }

}
