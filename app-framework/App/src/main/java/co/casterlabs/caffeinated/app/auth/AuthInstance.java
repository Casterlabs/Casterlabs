package co.casterlabs.caffeinated.app.auth;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.koi.api.KoiConnection;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.listener.KoiLifeCycleHandler;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.events.ViewerListEvent;
import co.casterlabs.koi.api.types.user.User;
import lombok.Getter;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AuthInstance implements KoiLifeCycleHandler, Closeable {
    private @Getter String tokenId;
    private String token;

    private FastLogger logger;
    private KoiConnection koi;

    private @Getter @Nullable User userData;
    private @Getter @Nullable StreamStatusEvent streamData;
    private @Getter @Nullable List<User> viewers;

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
        this.koi = new KoiConnection(
            KoiConnection.KOI_URL,
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
        CaffeinatedApp.getInstance().getAuth().getAuthInstances().remove(this.tokenId);
        CaffeinatedApp.getInstance().getAuth().checkAuth();
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    /* ---------------- */
    /* Event Listeners  */
    /* ---------------- */

    @KoiEventHandler
    public void onUserUpdate(UserUpdateEvent e) {
        // Update the logger with the streamer's name.
        this.logger = new FastLogger(String.format("AuthInstance (%d) %s", this.tokenId.hashCode(), e.getStreamer().getDisplayname()));
        this.userData = e.getStreamer();
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    @KoiEventHandler
    public void onStreamStatus(StreamStatusEvent e) {
        this.streamData = e;
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    @KoiEventHandler
    public void onViewerList(ViewerListEvent e) {
        this.viewers = e.getViewers();
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    @KoiEventHandler
    public void onEvent(KoiEvent e) {
        KoiEventUtil.reflectInvoke(CaffeinatedApp.getInstance().getKoi(), e);
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
