package co.casterlabs.caffeinated;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class BuildInfo {
    private String author;
    private String version;
    private String buildChannel;

    public String getVersionString() {
        return String.format("%s-%s", this.version, this.buildChannel);
    }

}
