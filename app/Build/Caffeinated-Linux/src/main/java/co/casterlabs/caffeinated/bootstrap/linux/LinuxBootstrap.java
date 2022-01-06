package co.casterlabs.caffeinated.bootstrap.linux;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.NativeSystemProvider;
import co.casterlabs.caffeinated.bootstrap.webview.impl.CefWebview;

public class LinuxBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        NativeSystemProvider.initialize(
            null,
            null,
            CefWebview.FACTORY
        );
        Bootstrap.main(args);
    }

}
