package co.casterlabs.caffeinated.app.networking.kinoko;

public interface KinokoV1Listener {

    public void onOpen();

    public void onOrphaned();

    public void onAdopted();

    public void onMessage(String message);

    public void onClose(boolean remote);

    default void onException(Exception e) {
        e.printStackTrace();
    }

}
