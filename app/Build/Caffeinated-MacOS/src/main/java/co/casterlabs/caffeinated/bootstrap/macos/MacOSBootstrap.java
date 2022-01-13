package co.casterlabs.caffeinated.bootstrap.macos;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.NativeSystemProvider;
import co.casterlabs.caffeinated.bootstrap.impl.WkWebview;

public class MacOSBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        NativeSystemProvider.initialize(
            true, // Only on macOS.
            new MacOSLafManager(),
            null,
            WkWebview.FACTORY
        );
        Bootstrap.main(args);
    }

}
