package co.casterlabs.caffeinated.updater;

import java.io.File;
import java.io.IOException;

import javax.swing.UIManager;

import net.harawata.appdirs.AppDirsFactory;

public class Launcher {
    public static final String appDataDir = AppDirsFactory.getInstance().getUserDataDir("casterlabs-caffeinated", null, null, true);
    public static final File bundleDirectory = new File(appDataDir, "app");

    static {
        bundleDirectory.mkdirs();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) throws IOException {
        UpdaterUI ui = new UpdaterUI();

    }

}
