package co.casterlabs.caffeinated.cef.bridge;

public interface DualConsumer<T, D> {

    public void accept(T type, D data);

}
