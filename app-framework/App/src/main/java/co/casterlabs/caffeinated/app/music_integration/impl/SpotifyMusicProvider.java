package co.casterlabs.caffeinated.app.music_integration.impl;

import java.io.IOException;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.app.music_integration.MusicProvider;
import co.casterlabs.caffeinated.app.music_integration.impl.SpotifyMusicProvider.SpotifySettings;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class SpotifyMusicProvider extends MusicProvider<SpotifySettings> {
    private static final String AUTH_PROXY_URL = "https://api.casterlabs.co/v2/natsukashii/spotify";

    private FastLogger logger = new FastLogger();

    /* Bridge.emit("auth:request-oauth-signin", { platform: "caffeinated_spotify", isKoi: false }); */
    public SpotifyMusicProvider(@NonNull MusicIntegration musicIntegration) {
        super("Spotify", "spotify", SpotifySettings.class);
        musicIntegration.getProviders().put(this.getServiceId(), this);

        CaffeinatedApp
            .getInstance()
            .onAppEvent("auth:completion", (JsonObject data) -> {
                if (data.getString("platform").equals("caffeinated_spotify")) {
                    logger.info("Completing OAuth.");
                    this.completeOAuth(data.getString("tokenId"));
                }
            });
    }

    private void completeOAuth(String tokenId) {
        String code = CaffeinatedApp.getInstance().getAuthPreferences().get().getTokens().getString(tokenId);

        try {
            JsonObject response = Rson.DEFAULT.fromJson(
                WebUtil.sendHttpRequest(
                    new Request.Builder()
                        .url(String.format("%s?code=%s", AUTH_PROXY_URL, code))
                ), JsonObject.class
            );

            String refreshToken = response.getString("refresh_token");

            // Update the auth file.
            CaffeinatedApp.getInstance().getAuthPreferences().get().getTokens().put(tokenId, refreshToken);
            CaffeinatedApp.getInstance().getAuthPreferences().save();

            FastLogger.logStatic("RefreshToken: ", refreshToken);
        } catch (JsonParseException | IOException e) {
            e.printStackTrace();
        }

        logger.info("OAuth Completed, enabling now.");

        CaffeinatedApp.getInstance().getAuthPreferences().save();
    }

    @Override
    protected void onSettingsUpdate() {

    }

    @JsonClass(exposeAll = true)
    public static class SpotifySettings {

    }

}
