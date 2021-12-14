package co.casterlabs.caffeinated.bootstrap.linux;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.theming.LafManager;

public class LinuxBootstrap {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        LafManager.initialize(null); // Linux does not have one.
        Bootstrap.main(args);
    }

}
