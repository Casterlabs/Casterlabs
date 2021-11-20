package co.casterlabs.caffeinated.bootstrap.events.window;

import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

public class AppWindowMinMaxEvent extends AbstractCancellableEvent<AppWindowEventType> {

    public AppWindowMinMaxEvent() {
        super(AppWindowEventType.MINMAX);
    }

}
