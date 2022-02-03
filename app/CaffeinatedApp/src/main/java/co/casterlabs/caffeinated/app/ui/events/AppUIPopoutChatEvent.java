package co.casterlabs.caffeinated.app.ui.events;

import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

public class AppUIPopoutChatEvent extends AbstractCancellableEvent<AppUIEventType> {

    public AppUIPopoutChatEvent() {
        super(AppUIEventType.POPOUT_CHAT);
    }

}
