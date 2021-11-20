package co.casterlabs.caffeinated.bootstrap.events.window;

import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

public class AppWindowCloseEvent extends AbstractCancellableEvent<AppWindowEventType> {

    public AppWindowCloseEvent() {
        super(AppWindowEventType.CLOSE);
    }

}
