package co.casterlabs.caffeinated.bootstrap.windows;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.NativeSystemProvider;
import co.casterlabs.caffeinated.bootstrap.webview.impl.CefWebview;
import co.casterlabs.caffeinated.bootstrap.windows.music.WindowsSystemPlaybackMusicProvider;

public class WindowsBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        NativeSystemProvider.initialize(
            false,
            new WindowsLafManager(),
            new WindowsSystemPlaybackMusicProvider(),
            CefWebview.FACTORY
        );
        Bootstrap.main(args);
    }

}
