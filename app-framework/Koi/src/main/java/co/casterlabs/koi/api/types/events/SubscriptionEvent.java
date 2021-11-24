package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class SubscriptionEvent extends Event {
    private User subscriber;

    @Deprecated
    private int months;

    @JsonField("gift_recipient")
    private User giftRecipient;

    @JsonField("sub_type")
    private SubscriptionType subType;

    @JsonField("sub_level")
    private SubscriptionLevel subLevel;

    @Override
    public EventType getType() {
        return EventType.SUBSCRIPTION;
    }

    public static enum SubscriptionType {
        SUB,
        RESUB,

        SUBGIFT,
        RESUBGIFT,

        ANONSUBGIFT,
        ANONRESUBGIFT;

    }

    public static enum SubscriptionLevel {
        UNKNOWN,
        TWITCH_PRIME,
        TIER_1,
        TIER_2,
        TIER_3,
        TIER_4,
        TIER_5;

    }

}
