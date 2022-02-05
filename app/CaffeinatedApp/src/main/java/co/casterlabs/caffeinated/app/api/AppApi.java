package co.casterlabs.caffeinated.app.api;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.networking.kinoko.KinokoV1Connection;
import co.casterlabs.caffeinated.app.networking.kinoko.KinokoV1Listener;
import co.casterlabs.rakurai.json.Rson;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AppApi implements KinokoV1Listener {

    private KinokoV1Connection connection = new KinokoV1Connection(this);

    public void init() {
        this.onClose(true);
    }

    public void sendSong() {
        if (this.connection.isConnected()) {
            this.connection.send(
                Rson.DEFAULT.toJson(
                    CaffeinatedApp
                        .getInstance()
                        .getMusic()
                        .getActivePlayback()
                ).toString()
            );
        }
    }

    @Override
    public void onMessage(String message) {
        switch (message) {

            case "get-song":
                this.sendSong();
                return;

        }
    }

    @Override
    public void onOpen() {
        this.sendSong();
        this.connection.getLogger().setCurrentLevel(LogLevel.INFO);
    }

    @SneakyThrows
    @Override
    public void onClose(boolean remote) {
        if (remote) {
            this.connection.connect(
                String.format(
                    "caffeinated_api:%s:music",
                    CaffeinatedApp.getInstance().getAppPreferences().get().getDeveloperApiKey()
                ),
                true,
                false
            );
        }
    }

    @Override
    public void onOrphaned() {}

    @Override
    public void onAdopted() {}

}
