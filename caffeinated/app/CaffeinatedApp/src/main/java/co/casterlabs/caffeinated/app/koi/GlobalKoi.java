package co.casterlabs.caffeinated.app.koi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.auth.AuthInstance;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.Command;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.listener.KoiLifeCycleHandler;
import co.casterlabs.koi.api.types.events.CatchupEvent;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.RoomstateEvent;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.events.ViewerCountEvent;
import co.casterlabs.koi.api.types.events.ViewerListEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@SuppressWarnings("deprecation")
public class GlobalKoi extends JavascriptObject implements Koi, KoiLifeCycleHandler {
    private static final List<KoiEventType> KEPT_EVENTS = Arrays.asList(
        KoiEventType.FOLLOW,
        KoiEventType.SUBSCRIPTION,
        KoiEventType.META,
        KoiEventType.VIEWER_JOIN,
        KoiEventType.VIEWER_LEAVE,
        KoiEventType.RAID,
        KoiEventType.CHANNEL_POINTS,
        KoiEventType.CLEARCHAT,
        KoiEventType.PLATFORM_MESSAGE,
        KoiEventType.RICH_MESSAGE,
        KoiEventType.LIKE,

        // Deprecated
        KoiEventType.CHAT,
        KoiEventType.DONATION
    );

    private List<KoiLifeCycleHandler> koiEventListeners = new LinkedList<>();

    // Definition hell, accessors for the UI.
    @JavascriptValue(allowSet = false)
    private List<KoiEvent> eventHistory = new LinkedList<>();

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private Map<UserPlatform, List<User>> viewers = new ConcurrentHashMap<>();

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private Map<UserPlatform, Integer> viewerCounts = new ConcurrentHashMap<>();

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private Map<UserPlatform, UserUpdateEvent> userStates = new ConcurrentHashMap<>();

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private Map<UserPlatform, StreamStatusEvent> streamStates = new ConcurrentHashMap<>();

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private Map<UserPlatform, RoomstateEvent> roomStates = new ConcurrentHashMap<>();

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private Map<UserPlatform, List<KoiIntegrationFeatures>> features = new ConcurrentHashMap<>();

    /**
     * @deprecated Should <b>only</b> be called from AppAuth.
     */
    @Deprecated
    public void updateFromAuth() {
        // Diff the AuthInstances and check for signedout platforms.
        List<UserPlatform> validPlatforms = new LinkedList<>();

        for (AuthInstance inst : CaffeinatedApp.getInstance().getAuth().getAuthInstances().values()) {
            if (inst.getUserData() != null) {
                validPlatforms.add(inst.getUserData().getPlatform());
            }
        }

        for (UserPlatform key : new ArrayList<>(this.userStates.keySet())) {
            if (!validPlatforms.contains(key)) {
                this.viewers.remove(key);
                this.userStates.remove(key);
                this.streamStates.remove(key);
                this.features.remove(key);
            }
        }

        this.updateBridgeData();
    }

    private void updateBridgeData() {
        for (AuthInstance auth : CaffeinatedApp.getInstance().getAuth().getAuthInstances().values()) {
            if (auth.getUserData() != null) {
                this.features.put(auth.getUserData().getPlatform(), Collections.unmodifiableList(auth.getFeatures()));
            }
        }

        // Send update to the widget instances.
        AsyncTask.create(() -> {
            JsonObject statics = this.toJson();
            JsonObject extendedStatics = this.toJsonExtended();

            for (CaffeinatedPlugin plugin : CaffeinatedApp.getInstance().getPlugins().getPlugins().getPlugins()) {
                for (Widget widget : plugin.getWidgets()) {
                    for (WidgetInstance instance : widget.getWidgetInstances()) {
                        try {
                            switch (instance.getInstanceMode()) {
                                case APPLET:
                                case DOCK:
                                case SETTINGS_APPLET:
                                    instance.onKoiStaticsUpdate(extendedStatics);
                                    break;

                                case DEMO:
                                case WIDGET:
                                case WIDGET_ALT:
                                    instance.onKoiStaticsUpdate(statics);
                                    break;
                            }
                        } catch (IOException ignored) {}
                    }
                }
            }
        });
    }

    @KoiEventHandler
    public void onEvent(KoiEvent e) {
        if (e.getType() == KoiEventType.CATCHUP) {
            CatchupEvent catchUp = (CatchupEvent) e;

            // Loop through the catchup events,
            // Convert them to an event,
            // Check to make sure that conversion succeeded,
            // Ensure that we're not spamming the user,
            // Broadcast that event.
            // (We need to do these in order)
            for (JsonElement element : catchUp.getEvents()) {
                KoiEvent cEvent = KoiEventType.get(element.getAsObject());

                if ((cEvent != null) && !this.eventHistory.contains(e)) {
                    if (KEPT_EVENTS.contains(cEvent.getType())) {
                        if (catchUp.isFresh()) {
                            this.eventHistory.add(cEvent);
                        } else {
                            this.onEvent(cEvent);
                        }
                    }
                }
            }
            return; // Don't further process.
        }

        // Process shout events.
        CaffeinatedApp.getInstance().getChatbot().processEventForShout(e);

        if (e.getType() == KoiEventType.RICH_MESSAGE) {
            ChatbotPreferences prefs = CaffeinatedApp.getInstance().getChatbotPreferences().get();
            RichMessageEvent richMessage = (RichMessageEvent) e;

            boolean commandResultedInAction = CaffeinatedApp.getInstance().getChatbot().processEventForCommand(richMessage);

            if (prefs.isHideCommandsFromChat()) {
                // Hide !commands and "commands".
                if (commandResultedInAction) {
                    return;
                }

                // Hide all command response messages.
                {
                    List<Command> commands = prefs.getCommands();

                    for (ChatbotPreferences.Command command : commands) {
                        if (command.getResponse().equals(richMessage.getRaw())) {
                            return;
                        }
                    }
                }
            }

            // We want to hide all messages from listed chatbots.
            {
                List<String> chatbots = prefs.getChatbots();

                for (String chatbot : chatbots) {
                    if (chatbot.equalsIgnoreCase(richMessage.getSender().getDisplayname())) {
                        return;
                    }
                }
            }

            if (prefs.isHideTimersFromChat()) {
                // Hide all timer messages.
                if (prefs.getTimers().contains(richMessage.getRaw())) {
                    return;
                }
            }
        }

        // Add it to the local event history.
        if (KEPT_EVENTS.contains(e.getType())) {
            this.eventHistory.add(e);
        }

        switch (e.getType()) {
            case VIEWER_COUNT: {
                this.viewerCounts.put(
                    e.getStreamer().getPlatform(),
                    ((ViewerCountEvent) e).getCount()
                );
                this.updateBridgeData();
                break;
            }

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

            case ROOMSTATE: {
                this.roomStates.put(
                    e.getStreamer().getPlatform(),
                    (RoomstateEvent) e
                );
                this.updateBridgeData();
                break;
            }

            default:
                break;
        }

        // Emit the event to Caffeinated.
        JsonElement asJson = Rson.DEFAULT.toJson(e);

        CaffeinatedApp.getInstance().getAppBridge().emit("koi:event:" + e.getType().name().toLowerCase(), asJson);
        CaffeinatedApp.getInstance().getAppBridge().emit("koi:event", asJson);

        // These are used internally.
        for (KoiLifeCycleHandler listener : this.koiEventListeners) {
            KoiEventUtil.reflectInvoke(listener, e);
        }

        // Notify the plugins
        for (CaffeinatedPlugin pl : CaffeinatedApp.getInstance().getPlugins().getPlugins().getPlugins()) {
            pl.fireKoiEventListeners(e);
        }

        FastLogger.logStatic(
            LogLevel.DEBUG,
            "Processed %s event for %s.",
            e.getType().name().toLowerCase().replace('_', ' '),
            e.getStreamer().getUPID()
        );
    }

    @JavascriptFunction
    @Override
    public void sendChat(@NonNull UserPlatform platform, @NonNull String message, @NonNull KoiChatterType chatter, @Nullable String replyTarget, boolean isUserGesture) {
        if (message.startsWith("/koi test")) {
            // NOOP
            return;
        }

        AuthInstance inst = CaffeinatedApp.getInstance().getAuth().getAuthInstance(platform);

        if (inst != null) {
            inst.sendChat(message, chatter, replyTarget, isUserGesture);
        }
    }

    @JavascriptFunction
    @Override
    public void upvoteChat(@NonNull UserPlatform platform, @NonNull String messageId) {
        AuthInstance inst = CaffeinatedApp.getInstance().getAuth().getAuthInstance(platform);

        if (inst != null) {
            inst.upvoteChat(messageId);
        }
    }

    @JavascriptFunction
    @Override
    public void deleteChat(@NonNull UserPlatform platform, @NonNull String messageId, boolean isUserGesture) {
        AuthInstance inst = CaffeinatedApp.getInstance().getAuth().getAuthInstance(platform);

        if (inst != null) {
            inst.deleteChat(messageId, isUserGesture);
        }
    }

    // These all have to be unmodifiable as they're exposed in the plugin SDK.

    @Override
    public List<KoiEvent> getEventHistory() {
        return Collections.unmodifiableList(this.eventHistory);
    }

    @Override
    public Map<UserPlatform, List<User>> getViewers() {
        return Collections.unmodifiableMap(this.viewers);
    }

    @Override
    public Map<UserPlatform, Integer> getViewerCounts() {
        return Collections.unmodifiableMap(this.viewerCounts);
    }

    @Override
    public Map<UserPlatform, UserUpdateEvent> getUserStates() {
        return Collections.unmodifiableMap(this.userStates);
    }

    @Override
    public Map<UserPlatform, StreamStatusEvent> getStreamStates() {
        return Collections.unmodifiableMap(this.streamStates);
    }

    @Override
    public Map<UserPlatform, RoomstateEvent> getRoomStates() {
        return Collections.unmodifiableMap(this.roomStates);
    }

    @Override
    public Map<UserPlatform, List<KoiIntegrationFeatures>> getFeatures() {
        return Collections.unmodifiableMap(this.features);
    }

}
