package co.casterlabs.koi.api;

public enum KoiIntegrationFeatures {

    /* ---- Misc ---- */

    CHANNEL_POINTS,
    CHAT_BOT_LINKING,
    UPDATE_STREAM_INFO,
    UPDATE_STREAM_THUMBNAIL,
    UPDATE_ROOM_STATE,

    /* ---- Alerts / Counts / Events ---- */

    DONATION_ALERT,
    FOLLOWER_ALERT,
    SUBSCRIPTION_ALERT,
    RAID_ALERT,

    FOLLOWER_COUNT,
    SUBSCRIBER_COUNT,

    CHAT,
    STREAM_STATUS,
    ROOMSTATE,

    /* ---- Viewers ---- */

    VIEWERS_LIST,
    VIEWERS_COUNT,
    VIEWERS_PRESENCE,

    /* ---- Chat & Moderation ---- */

    MESSAGE_UPVOTE,
    MESSAGE_DELETION,
    MESSAGE_REACTION,

    CHAT_SEND_MESSAGE,
    CHAT_SEND_COMMAND,

    ;

}
