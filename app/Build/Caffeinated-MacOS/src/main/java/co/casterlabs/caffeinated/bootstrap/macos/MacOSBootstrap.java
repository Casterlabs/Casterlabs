package co.casterlabs.caffeinated.bootstrap.macos;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.NativeSystemProvider;
import co.casterlabs.caffeinated.bootstrap.webview.impl.CefWebview;

public class MacOSBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        NativeSystemProvider.initialize(
            new MacOSLafManager(),
            null,
            CefWebview.FACTORY
        );
        Bootstrap.main(args);
    }

}
