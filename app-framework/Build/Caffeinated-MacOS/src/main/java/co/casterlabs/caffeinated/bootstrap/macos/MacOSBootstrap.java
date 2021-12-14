package co.casterlabs.caffeinated.bootstrap.macos;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.theming.LafManager;

public class MacOSBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        LafManager.initialize(new MacOSLafManager());
        Bootstrap.main(args);
    }

}
