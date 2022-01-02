package co.casterlabs.caffeinated.bootstrap.windows;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.NativeSystemProvider;
import co.casterlabs.caffeinated.bootstrap.windows.music.WindowsSystemPlaybackMusicProvider;

public class WindowsBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        NativeSystemProvider.initialize(new WindowsLafManager(), new WindowsSystemPlaybackMusicProvider());
        Bootstrap.main(args);
    }

}
