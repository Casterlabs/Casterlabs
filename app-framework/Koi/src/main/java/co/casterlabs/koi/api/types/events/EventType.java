package co.casterlabs.koi.api.types.events;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.AllArgsConstructor;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@AllArgsConstructor
public enum EventType {
    // @formatter:off
    FOLLOW           (FollowEvent.class),
    CHAT             (ChatEvent.class),
    DONATION         (DonationEvent.class),
    SUBSCRIPTION     (SubscriptionEvent.class),
    USER_UPDATE      (UserUpdateEvent.class),
    STREAM_STATUS    (StreamStatusEvent.class),
    META             (MessageMetaEvent.class),
    VIEWER_JOIN      (ViewerJoinEvent.class),
    VIEWER_LEAVE     (ViewerLeaveEvent.class),
    VIEWER_LIST      (ViewerListEvent.class),
    RAID             (RaidEvent.class),
    CHANNEL_POINTS   (ChannelPointsEvent.class),
    CATCHUP          (CatchupEvent.class),
    CLEARCHAT        (ClearChatEvent.class);
    // @formatter:on

    private Class<? extends Event> eventClass;

    public static Event get(JsonObject eventJson) {
        String eventType = eventJson.getString("event_type");

        try {
            // 1) Lookup the event type
            // 2) Use RSON to deserialize to object using the eventClass.
            // 3) Profit!
            EventType type = EventType.valueOf(eventType);
            Event event = Rson.DEFAULT.fromJson(eventJson, type.eventClass);

            return event;
        } catch (IllegalArgumentException e) {
            return null;
        } catch (JsonParseException e) {
            FastLogger.logStatic(LogLevel.SEVERE, "An error occured while converting an event of type %s", eventType);
            FastLogger.logException(e);
            return null;
        }
    }

}
