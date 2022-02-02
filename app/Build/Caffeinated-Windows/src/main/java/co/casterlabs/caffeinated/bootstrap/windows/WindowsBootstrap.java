package co.casterlabs.caffeinated.bootstrap.windows;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.NativeSystem;
import co.casterlabs.caffeinated.bootstrap.windows.music.WindowsSystemPlaybackMusicProvider;
import co.casterlabs.caffeinated.webview.impl.CefWebview;

public class WindowsBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        NativeSystem.initialize(
            false,
            new WindowsLafManager(),
            new WindowsSystemPlaybackMusicProvider(),
            CefWebview.FACTORY
        );
        Bootstrap.main(args);
    }

}
