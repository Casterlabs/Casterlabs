package co.casterlabs.caffeinated.app;

import co.casterlabs.caffeinated.app.networking.localserver.LocalServer;
import co.casterlabs.caffeinated.util.Crypto;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class AppPreferences {
    private int conductorPort = LocalServer.DEFAULT_PORT;
    private String conductorKey = new String(Crypto.generateSecureRandomKey());

}
