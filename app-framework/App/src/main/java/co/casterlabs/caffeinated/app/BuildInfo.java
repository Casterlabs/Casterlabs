package co.casterlabs.caffeinated.app;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class BuildInfo {
    private String author;
    private String version;
    private String buildChannel;
    private String versionString;
    private boolean isDev = false;

    @JsonValidate
    private void validate() {
        if (this.version.startsWith("${")) {
            this.buildChannel = "dev";
            this.version = "0.0.0";
            this.versionString = "dev-build";
            this.isDev = true;
        } else {
            this.versionString = String.format("%s-%s", this.version, this.buildChannel);
        }
    }

}
