package co.casterlabs.caffeinated.bootstrap.events.window;

import xyz.e3ndr.eventapi.events.AbstractCancellableEvent;

public class AppWindowThemeLoadedEvent extends AbstractCancellableEvent<AppWindowEventType> {

    public AppWindowThemeLoadedEvent() {
        super(AppWindowEventType.THEME_LOADED);
    }

}
