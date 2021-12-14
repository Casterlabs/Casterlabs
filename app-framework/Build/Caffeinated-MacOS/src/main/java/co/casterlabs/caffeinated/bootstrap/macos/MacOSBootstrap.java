package co.casterlabs.caffeinated.bootstrap.macos;

import co.casterlabs.caffeinated.bootstrap.Test;
import co.casterlabs.caffeinated.bootstrap.theming.LafManager;

public class MacOSBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        LafManager.initialize(new MacOSLafManager());
        Test.main(args);
    }

}
