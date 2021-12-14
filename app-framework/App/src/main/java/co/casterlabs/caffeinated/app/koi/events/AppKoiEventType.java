package co.casterlabs.caffeinated.app.koi.events;

import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractEvent;

public enum AppKoiEventType {
    CHATSEND(AppKoiChatSendEvent.class),
    UPVOTE(AppKoiUpvoteEvent.class);

    private @Getter Class<AbstractEvent<AppKoiEventType>> eventClass;

    @SuppressWarnings("unchecked")
    private AppKoiEventType(Class<?> clazz) {
        this.eventClass = (Class<AbstractEvent<AppKoiEventType>>) clazz;
    }

}
