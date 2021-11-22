package co.casterlabs.caffeinated.app.auth;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.koi.api.Koi;
import co.casterlabs.koi.api.listener.EventHandler;
import co.casterlabs.koi.api.listener.EventListener;
import co.casterlabs.koi.api.listener.EventUtil;
import co.casterlabs.koi.api.types.events.Event;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.events.ViewerListEvent;
import co.casterlabs.koi.api.types.user.User;
import lombok.Getter;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AuthInstance implements EventListener, Closeable {
    private @Getter String tokenId;
    private String token;

    private FastLogger logger;
    private Koi koi;

    private @Getter @Nullable User userData;
    private @Getter @Nullable StreamStatusEvent streamData;
    private @Getter @Nullable List<User> viewers;

    // TODO global koi.
    public AuthInstance(String tokenId) {
        this.tokenId = tokenId;

        this.logger = new FastLogger(String.format("AuthInstance (%d) ?", this.tokenId.hashCode()));

        this.token = CaffeinatedApp
            .getInstance()
            .getAuthPreferences()
            .get()
            .getKoiTokens()
            .getString(this.tokenId);

        FastLogger koiLogger = new FastLogger("AuthInstance Koi");
        koiLogger.setCurrentLevel(LogLevel.SEVERE);
        this.koi = new Koi(
            Koi.KOI_URL,
            koiLogger,
            this,
            CaffeinatedApp.caffeinatedClientId
        );

        this.reconnect();
    }

    public void invalidate() {
        try {
            this.close();
        } catch (IOException ignored) {}

        this.logger.info("I have been invalidate()'d, goodbye.");
        CaffeinatedApp.getInstance().getAuthPreferences().get().getKoiTokens().remove(this.tokenId);
        CaffeinatedApp.getInstance().getAuthPreferences().save();
    }

    /* ---------------- */
    /* Event Listeners  */
    /* ---------------- */

    @EventHandler
    public void onUserUpdate(UserUpdateEvent e) {
        // Update the logger with the streamer's name.
        this.logger = new FastLogger(String.format("AuthInstance (%d) %s", this.tokenId.hashCode(), e.getStreamer().getDisplayname()));
        this.userData = e.getStreamer();
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    @EventHandler
    public void onStreamStatus(StreamStatusEvent e) {
        this.streamData = e;
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    @EventHandler
    public void onViewerList(ViewerListEvent e) {
        this.viewers = e.getViewers();
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    @EventHandler
    public void onEvent(Event e) {
        EventUtil.reflectInvoke(CaffeinatedApp.getInstance().getKoi(), e);
    }

    /* ---------------- */
    /* Connection Stuff */
    /* ---------------- */

    @SneakyThrows
    private void reconnect() {
        this.koi.login(this.token);
    }

    @Override
    public void onClose(boolean remote) {
        if (remote) {
            this.logger.info("Reconnecting to Koi.");
            this.reconnect();
        }
    }

    @Override
    public void onServerMessage(String message) {
        this.logger.info("Server message: %s", message);
    }

    @Override
    public void onException(Exception e) {
        this.logger.exception(e);
    }

    @Override
    public void close() throws IOException {
        this.koi.close();
    }

    public boolean isConnected() {
        return this.koi.isConnected();
    }

}
