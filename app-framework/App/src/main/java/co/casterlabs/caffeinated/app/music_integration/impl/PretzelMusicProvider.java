package co.casterlabs.caffeinated.app.music_integration.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.app.music_integration.MusicProvider;
import co.casterlabs.caffeinated.app.music_integration.MusicTrack;
import co.casterlabs.caffeinated.app.music_integration.impl.PretzelMusicProvider.PretzelSettings;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class PretzelMusicProvider extends MusicProvider<PretzelSettings> {
    private static final String PRETZEL_ALBUM_ART = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mOU3rzkPwAE1gJzt/4W2gAAAABJRU5ErkJggg==";
    private static final String PRETZEL_ENDPOINT = "https://api.pretzel.tv/playing/twitch/%s";
    private static final long POLL_RATE = TimeUnit.SECONDS.toMillis(20);

    private FastLogger logger = new FastLogger();

    private String channelId;
    private MusicTrack currentTrackCache;

    public PretzelMusicProvider(@NonNull MusicIntegration musicIntegration) {
        super("Pretzel", "pretzel", PretzelSettings.class);
        musicIntegration.getProviders().put(this.getServiceId(), this);

        CaffeinatedApp.getInstance().onAppEvent("auth:platforms", (JsonObject data) -> {
            JsonObject koiAuth = data.getObject("koiAuth");

            if (koiAuth.containsKey("TWITCH")) {
                JsonObject twitchUserData = koiAuth
                    .getObject("TWITCH")
                    .getObject("userData");

                String id = twitchUserData.getString("channel_id");

                if (!this.channelId.equals(id)) {
                    this.logger.info("Now signed in as: %s", twitchUserData.getString("displayname"));

                    this.channelId = id;

                    this.setAccountData(
                        true,
                        String.format("Twitch: %s", twitchUserData.getString("displayname")),
                        "https://play.pretzel.rocks"
                    );

                    new AsyncTask(() -> {
                        this.pollPretzel();
                    });
                }
            } else {
                this.logger.info("Signed out.");
                this.channelId = null;
                this.setAccountData(false, null, null);
            }
        });

        new AsyncTask(() -> {
            while (true) {
                this.pollPretzel();
                try {
                    Thread.sleep(POLL_RATE);
                } catch (InterruptedException ignored) {}
            }
        });
    }

    private void pollPretzel() {
        if (this.isSignedIn()) {
            try {
                String response = WebUtil.sendHttpRequest(new Request.Builder().url(String.format(PRETZEL_ENDPOINT, this.channelId)));

                // Janky check for ratelimit.
                if (response.startsWith("Now Playing: ")) {
                    // "Now Playing: Arcade by Stesso -> https://prtzl.io/PDHXD5z6enygxyFmE"
//                    this.logger.debug(response);

                    // "Arcade by Stesso -> https://prtzl.io/PDHXD5z6enygxyFmE"
                    String nowPlaying = response.substring("Now Playing: ".length());

                    // Separate out the link
                    String[] nowPlayingArr = nowPlaying.split(" -> https://");
                    String songLink = "https://" + nowPlayingArr[1];

                    // "Arcade by Stesso"
                    // Split out the title and artist(s)
                    String[] songNameArr = nowPlayingArr[0].split(" by ");

                    String title = songNameArr[0];
                    List<String> artists = Arrays.asList(songNameArr[1]);

                    // *thanos meme*
                    // All that, for a music track?
                    this.currentTrackCache = new MusicTrack(
                        title,
                        artists,
                        null,
                        PRETZEL_ALBUM_ART,
                        songLink
                    );

                    // this.logger.info("Polled Pretzel successfully.");
                } /* else {
                     this.logger.debug("Error while polling Pretzel: %s", response);
                  }*/

                if (this.isEnabled() && (this.currentTrackCache != null)) {
                    this.setPlaying(this.currentTrackCache);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.currentTrackCache = null;
        }
    }

    @Override
    protected void onSettingsUpdate() {
        if (this.isEnabled()) {
            this.pollPretzel();
        } else {
            this.setPlaybackStateInactive();
        }
    }

    @Override
    public void signout() {} // NO-OP

    public boolean isEnabled() {
        return this.getSettings().enabled;
    }

    @JsonClass(exposeAll = true)
    public static class PretzelSettings {
        private boolean enabled = false;

    }

}
