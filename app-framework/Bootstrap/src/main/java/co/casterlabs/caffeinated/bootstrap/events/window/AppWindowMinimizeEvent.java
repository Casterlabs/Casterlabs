package co.casterlabs.caffeinated.bootstrap.events.window;

import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

public class AppWindowMinimizeEvent extends AbstractCancellableEvent<AppWindowEventType> {

    public AppWindowMinimizeEvent() {
        super(AppWindowEventType.MINIMIZE);
    }

}
