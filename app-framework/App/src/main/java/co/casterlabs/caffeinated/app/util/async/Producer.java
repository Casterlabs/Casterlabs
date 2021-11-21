package co.casterlabs.caffeinated.app.util.async;

import org.jetbrains.annotations.Nullable;

public interface Producer<T> {

    public @Nullable T produce() throws InterruptedException;

}
