package co.casterlabs.caffeinated.app.plugins.events;

import lombok.Getter;
import xyz.e3ndr.eventapi.events.AbstractEvent;

public enum AppPluginIntegrationEventType {
    WIDGET_CREATE(AppPluginIntegrationCreateWidgetEvent.class);

    private @Getter Class<AbstractEvent<AppPluginIntegrationEventType>> eventClass;

    @SuppressWarnings("unchecked")
    private AppPluginIntegrationEventType(Class<?> clazz) {
        this.eventClass = (Class<AbstractEvent<AppPluginIntegrationEventType>>) clazz;
    }

}
