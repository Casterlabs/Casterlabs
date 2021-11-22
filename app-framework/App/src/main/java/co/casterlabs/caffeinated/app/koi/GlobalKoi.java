package co.casterlabs.caffeinated.app.koi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.casterlabs.caffeinated.app.AppBridge;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.auth.AuthInstance;
import co.casterlabs.koi.api.listener.EventHandler;
import co.casterlabs.koi.api.listener.EventListener;
import co.casterlabs.koi.api.listener.EventUtil;
import co.casterlabs.koi.api.types.events.CatchupEvent;
import co.casterlabs.koi.api.types.events.Event;
import co.casterlabs.koi.api.types.events.EventType;
import co.casterlabs.koi.api.types.events.ViewerListEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@Getter
public class GlobalKoi implements EventListener {
    private static final List<EventType> HISTORY_EVENTS = Arrays.asList();

    private List<EventListener> koiEventListeners = new LinkedList<>();

    private List<Event> chatHistory = new LinkedList<>();
    private Map<String, List<User>> viewers = new HashMap<>();

    /**
     * Should *only* be called from AppAuth.
     */
    @Deprecated
    public void updateFromAuth() {
        List<String> validPlatforms = new LinkedList<>();

        for (AuthInstance inst : CaffeinatedApp.getInstance().getAuth().getAuthInstances().values()) {
            if (inst.getUserData() != null) {
                validPlatforms.add(inst.getUserData().getPlatform().name());
            }
        }

        for (String key : new ArrayList<>(this.viewers.keySet())) {
            if (!validPlatforms.contains(key)) {
                this.viewers.remove(key);
            }
        }

        this.updateBridgeData();
    }

    private void updateBridgeData() {
        JsonObject bridgeData = new JsonObject()
            .put("chatHistory", Rson.DEFAULT.toJson(this.chatHistory))
            .put("viewers", Rson.DEFAULT.toJson(this.viewers));

        AppBridge bridge = CaffeinatedApp.getInstance().getBridge();

        bridge.emit("koi:chatHistory", bridgeData.get("chatHistory"));
        bridge.emit("koi:viewers", bridgeData.get("viewers"));

        bridge.getQueryData().put("koi", bridgeData);
    }

    @EventHandler
    public void onEvent(Event e) {
        if (e.getType() == EventType.CATCHUP) {
            CatchupEvent catchUp = (CatchupEvent) e;

            // Loop through the catchup events,
            // Convert them to an event,
            // Check to make sure that conversion succeeded,
            // Broadcast that event.
            // (We need to do these in order)
            for (JsonElement element : catchUp.getEvents()) {
                Event cEvent = EventType.get(element.getAsObject());

                if (cEvent != null) {
                    this.onEvent(cEvent);
                }
            }
        } else {
            if (HISTORY_EVENTS.contains(e.getType())) {
                this.chatHistory.add(e);
                this.updateBridgeData();
            } else if (e.getType() == EventType.VIEWER_LIST) {
                this.viewers.put(
                    e.getStreamer().getPlatform().name(),
                    Collections.unmodifiableList(((ViewerListEvent) e).getViewers())
                );
            }

            // Emit the event to Caffeinated.
            CaffeinatedApp.getInstance().getBridge().emit("koi:event:" + e.getType().name().toLowerCase(), Rson.DEFAULT.toJson(e));

            for (EventListener listener : this.koiEventListeners) {
                EventUtil.reflectInvoke(listener, e);
            }
        }

        FastLogger.logStatic(LogLevel.DEBUG, "Processed event for %s: %s", e.getStreamer().getDisplayname(), e);
    }

}
