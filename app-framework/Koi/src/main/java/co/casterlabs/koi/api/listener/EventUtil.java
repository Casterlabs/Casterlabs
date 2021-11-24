package co.casterlabs.koi.api.listener;

import java.lang.reflect.Method;

import lombok.NonNull;
import lombok.SneakyThrows;

public class EventUtil {

    @SneakyThrows
    public static void reflectInvoke(@NonNull EventListener listener, @NonNull Object event) {
        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventHandler.class) &&
                (method.getParameterCount() == 1) &&
                method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                method.invoke(listener, event);
            }
        }
    }

}
