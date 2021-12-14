package co.casterlabs.caffeinated.bootstrap.windows;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.ui.LafManager;

public class WindowsBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        LafManager.initialize(new WindowsLafManager());
        Bootstrap.main(args);
    }

}
