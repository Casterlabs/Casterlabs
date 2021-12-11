package co.casterlabs.caffeinated.app.koi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.casterlabs.caffeinated.app.AppBridge;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.auth.AuthInstance;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.listener.KoiLifeCycleHandler;
import co.casterlabs.koi.api.types.events.CatchupEvent;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.events.ViewerListEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@Getter
public class GlobalKoi implements KoiLifeCycleHandler {
    private List<KoiLifeCycleHandler> koiEventListeners = new LinkedList<>();

    private List<KoiEvent> eventHistory = new LinkedList<>();
    private Map<UserPlatform, List<User>> viewers = new HashMap<>();
    private Map<UserPlatform, UserUpdateEvent> userStates = new HashMap<>();
    private Map<UserPlatform, StreamStatusEvent> streamStates = new HashMap<>();

    @SneakyThrows
    public void init() {
        // Set read-only pointers to this instance's chatHistory and viewers fields.
        ReflectionLib.setStaticValue(Koi.class, "eventHistory", Collections.unmodifiableList(this.eventHistory));
        ReflectionLib.setStaticValue(Koi.class, "viewers", Collections.unmodifiableMap(this.viewers));
        ReflectionLib.setStaticValue(Koi.class, "userStates", Collections.unmodifiableMap(this.userStates));
        ReflectionLib.setStaticValue(Koi.class, "streamStates", Collections.unmodifiableMap(this.streamStates));
    }

    /**
     * @deprecated Should <b>only</b> be called from AppAuth.
     */
    @Deprecated
    public void updateFromAuth() {
        List<UserPlatform> validPlatforms = new LinkedList<>();

        for (AuthInstance inst : CaffeinatedApp.getInstance().getAuth().getAuthInstances().values()) {
            if (inst.getUserData() != null) {
                validPlatforms.add(inst.getUserData().getPlatform());
            }
        }

        for (UserPlatform key : new ArrayList<>(this.viewers.keySet())) {
            if (!validPlatforms.contains(key)) {
                this.viewers.remove(key);
                this.userStates.remove(key);
                this.streamStates.remove(key);
            }
        }

        this.updateBridgeData();
    }

    private void updateBridgeData() {
        JsonObject bridgeData = new JsonObject()
            .put("history", Rson.DEFAULT.toJson(this.eventHistory))
            .put("viewers", Rson.DEFAULT.toJson(this.viewers))
            .put("userStates", Rson.DEFAULT.toJson(this.userStates))
            .put("streamStates", Rson.DEFAULT.toJson(this.streamStates));

        AppBridge bridge = CaffeinatedApp.getInstance().getBridge();

        bridge.emit("koi:viewers", bridgeData.get("viewers"));
        bridge.emit("koi:userStates", bridgeData.get("userStates"));
        bridge.emit("koi:streamStates", bridgeData.get("streamStates"));

        bridge.getQueryData().put("koi", bridgeData);

        new AsyncTask(() -> {
            for (CaffeinatedPlugin plugin : CaffeinatedApp.getInstance().getPlugins().getPlugins().getPlugins()) {
                for (Widget widget : plugin.getWidgets()) {
                    for (WidgetInstance instance : widget.getWidgetInstances()) {
                        try {
                            instance.onKoiStaticsUpdate(bridgeData);
                        } catch (IOException ignored) {}
                    }
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    @KoiEventHandler
    public void onEvent(KoiEvent e) {
        if (e.getType() == KoiEventType.CATCHUP) {
            CatchupEvent catchUp = (CatchupEvent) e;

            // Loop through the catchup events,
            // Convert them to an event,
            // Check to make sure that conversion succeeded,
            // Broadcast that event.
            // (We need to do these in order)
            for (JsonElement element : catchUp.getEvents()) {
                KoiEvent cEvent = KoiEventType.get(element.getAsObject());

                if (cEvent != null) {
                    this.onEvent(cEvent);
                }
            }
        } else {
            this.eventHistory.add(e);

            switch (e.getType()) {

                case VIEWER_LIST: {
                    this.viewers.put(
                        e.getStreamer().getPlatform(),
                        Collections.unmodifiableList(((ViewerListEvent) e).getViewers())
                    );

                    this.updateBridgeData();
                    break;
                }

                case USER_UPDATE: {
                    this.userStates.put(
                        e.getStreamer().getPlatform(),
                        (UserUpdateEvent) e
                    );

                    this.updateBridgeData();
                    break;
                }

                case STREAM_STATUS: {
                    this.streamStates.put(
                        e.getStreamer().getPlatform(),
                        (StreamStatusEvent) e
                    );

                    this.updateBridgeData();
                    break;
                }

                default:
                    break;
            }

            // Emit the event to Caffeinated.
            JsonElement asJson = Rson.DEFAULT.toJson(e);

            CaffeinatedApp.getInstance().getBridge().emit("koi:event:" + e.getType().name().toLowerCase(), asJson);
            CaffeinatedApp.getInstance().getBridge().emit("koi:event", asJson);

            // These are used internally.
            for (KoiLifeCycleHandler listener : this.koiEventListeners) {
                KoiEventUtil.reflectInvoke(listener, e);
            }

            // Notify the plugins
            for (CaffeinatedPlugin pl : CaffeinatedApp.getInstance().getPlugins().getPlugins().getPlugins()) {
                pl.fireKoiEventListeners(e);
            }
        }

        FastLogger.logStatic(
            LogLevel.DEBUG,
            "Processed %s event for %s.",
            e.getType().name().toLowerCase().replace('_', ' '),
            e.getStreamer().getDisplayname()
        );
    }

}
