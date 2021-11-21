package co.casterlabs.caffeinated.app.auth;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.auth.events.AppAuthCancelSigninEvent;
import co.casterlabs.caffeinated.app.auth.events.AppAuthEventType;
import co.casterlabs.caffeinated.app.auth.events.AppAuthRequestOAuthSigninEvent;
import co.casterlabs.caffeinated.app.auth.events.AppAuthSignoutEvent;
import co.casterlabs.caffeinated.app.networking.kinoko.AuthCallback;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class AppAuth {
    private static EventHandler<AppAuthEventType> handler = new EventHandler<>();

    private FastLogger logger = new FastLogger();

    private AuthCallback currentAuthCallback;

    private @Getter Map<String, AuthInstance> authInstances = new HashMap<>();

    private @Getter boolean isAuthorized = false;

    public AppAuth() {
        handler.register(this);
    }

    public boolean isSignedIn() {
        return !this.authInstances.isEmpty();
    }

    public void init() {
        this.updateBridgeData(); // Populate

        List<String> ids = CaffeinatedApp.getInstance().getAuthPreferences().get().getKoiTokenIds();

        for (String tokenId : ids) {
            this.startAuthInstance(tokenId);
        }
    }

    public void shutdown() {
        this.onCancelSigninEvent(null);

        for (AuthInstance inst : this.authInstances.values()) {
            try {
                inst.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int countPlatform(UserPlatform platform) {
        int count = 0;
        for (AuthInstance inst : this.authInstances.values()) {
            if ((inst.getUserData() != null) && (inst.getUserData().getPlatform() == platform)) {
                count++;
            }
        }
        return count;
    }

    public void checkAuth() {
        boolean authorized = false;

        for (AuthInstance inst : this.authInstances.values()) {
            if (inst.isConnected() && (inst.getUserData() != null)) {
                authorized = true;
                break;
            }
        }

        this.isAuthorized = authorized;

        CaffeinatedApp
            .getInstance()
            .getAppearanceManager()
            .navigate(this.isAuthorized ? "/home" : "/signin");
    }

    public void updateBridgeData() {
        this.checkAuth();

        JsonArray koiAuth = new JsonArray();

        for (AuthInstance inst : this.authInstances.values()) {
            koiAuth.add(
                new JsonObject()
                    .put("tokenId", inst.getTokenId())
                    .put("userData", Rson.DEFAULT.toJson(inst.getUserData()))
                    .put("streamData", Rson.DEFAULT.toJson(inst.getStreamData()))
            );
        }

        JsonObject bridgeData = new JsonObject()
            .put("isAuthorized", this.isAuthorized)
            .put("koiAuth", koiAuth);

        CaffeinatedApp.getInstance().getBridge().getQueryData().put("auth", bridgeData);
    }

    private void startAuthInstance(String tokenId) {
        this.logger.debug("Starting AuthInstance with id: %s", tokenId);
        this.authInstances.put(tokenId, new AuthInstance(tokenId));
    }

    @EventListener
    public void onRequestOAuthSigninEvent(AppAuthRequestOAuthSigninEvent event) {
        try {
            this.logger.info("Signin requested. (%s)", event.getPlatform());

            if (this.currentAuthCallback != null) {
                this.onCancelSigninEvent(null);
            }

            this.currentAuthCallback = this.authorize(event.getPlatform());

            this.currentAuthCallback
                .connect()
                .then((token) -> {
                    if (token != null) {
                        this.logger.info("Signin completed (%s)", event.getPlatform());

                        if (event.isKoi()) {
                            String tokenId = CaffeinatedApp
                                .getInstance()
                                .getAuthPreferences()
                                .get()
                                .addKoiToken(token);

                            this.startAuthInstance(tokenId);
                        } else {
                            CaffeinatedApp
                                .getInstance()
                                .getAuthPreferences()
                                .get()
                                .addToken(event.getPlatform(), token);

                            CaffeinatedApp.getInstance().emitAppEvent(
                                "auth:completion",
                                new JsonObject()
                                    .put("platform", event.getPlatform())
                                    .put("tokenId", event.getPlatform())
                            );
                        }
                    }

                    this.currentAuthCallback = null;
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventListener
    public void onSignoutEvent(AppAuthSignoutEvent event) {

    }

    @EventListener
    public void onCancelSigninEvent(AppAuthCancelSigninEvent event) {
        this.logger.info("Signin cancelled (?)");
        if (this.currentAuthCallback != null) {
            this.currentAuthCallback.cancel();
            this.currentAuthCallback = null;
        }
    }

    public AuthCallback authorize(String type) throws IOException {
        String oauthLink = CaffeinatedApp.AUTH_URLS.getString(type);

        if (oauthLink == null) {
            throw new IllegalArgumentException("Type '" + type + "' does not have an oauth link associated with it.");
        } else {
            AuthCallback callback = new AuthCallback(type);

            try {
                Desktop
                    .getDesktop()
                    .browse(new URI(oauthLink + callback.getStateString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            return callback;
        }
    }

    public static void invokeEvent(JsonObject data, String nestedType) throws InvocationTargetException, JsonParseException {
        handler.call(
            Rson.DEFAULT.fromJson(
                data,
                AppAuthEventType
                    .valueOf(nestedType)
                    .getEventClass()
            )
        );
    }

}
