package co.casterlabs.caffeinated.bootstrap.events.window;

import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractEvent;

public enum AppWindowEventType {
    MINIMIZE(AppWindowMinimizeEvent.class),
    MINMAX(AppWindowMinMaxEvent.class),
    CLOSE(AppWindowCloseEvent.class),
    THEME_LOADED(AppWindowThemeLoadedEvent.class);

    private @Getter Class<AbstractEvent<AppWindowEventType>> eventClass;

    @SuppressWarnings("unchecked")
    private AppWindowEventType(Class<?> clazz) {
        this.eventClass = (Class<AbstractEvent<AppWindowEventType>>) clazz;
    }

}
