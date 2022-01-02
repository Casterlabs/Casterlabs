package co.casterlabs.caffeinated.app;

import co.casterlabs.caffeinated.util.Crypto;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class AppPreferences {
    private static int override_conductorPort = -1;

    private int conductorPort = 8092; // Caffeinated <1.2 was 8091.
    private String conductorKey = new String(Crypto.generateSecureRandomKey());
    private boolean isNew = true;
    private boolean showDeveloperFeatures = false;

    static {
        String overridePort = System.getProperty("caffeinated.conductor.overrideport");

        if (overridePort != null) {
            override_conductorPort = Integer.parseInt(overridePort);
        }
    }

    public int getConductorPort() {
        return (override_conductorPort == -1) ? this.conductorPort : override_conductorPort;
    }

}
