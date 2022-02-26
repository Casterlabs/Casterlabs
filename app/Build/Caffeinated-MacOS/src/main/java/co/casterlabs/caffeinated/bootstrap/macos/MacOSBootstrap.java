package co.casterlabs.caffeinated.bootstrap.macos;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.NativeSystem;

public class MacOSBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        NativeSystem.initialize(
            true, // Only on macOS.
            null
        );
        Bootstrap.main(args);
    }

}
