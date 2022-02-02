package co.casterlabs.caffeinated.bootstrap.macos;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.NativeSystem;
import co.casterlabs.caffeinated.webview.impl.WkWebview;

public class MacOSBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        NativeSystem.initialize(
            true, // Only on macOS.
            new MacOSLafManager(),
            null,
            WkWebview.FACTORY
        );
        Bootstrap.main(args);
    }

}
