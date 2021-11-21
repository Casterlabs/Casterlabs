package co.casterlabs.caffeinated.util.async;

import org.jetbrains.annotations.Nullable;

public interface Producer<T> {

    public @Nullable T produce() throws InterruptedException;

    public static <T> Producer<T> of(@Nullable T value) {
        return () -> {
            return value;
        };
    }

}
