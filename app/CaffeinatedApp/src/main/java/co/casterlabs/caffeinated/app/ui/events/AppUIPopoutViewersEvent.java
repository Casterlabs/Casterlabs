package co.casterlabs.caffeinated.app.ui.events;

import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

public class AppUIPopoutViewersEvent extends AbstractCancellableEvent<AppUIEventType> {

    public AppUIPopoutViewersEvent() {
        super(AppUIEventType.POPOUT_VIEWERS);
    }

}
