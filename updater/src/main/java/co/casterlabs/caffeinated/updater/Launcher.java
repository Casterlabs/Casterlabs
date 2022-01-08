package co.casterlabs.caffeinated.updater;

import java.io.File;

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

    public static void main(String[] args) throws Exception {
        UpdaterUI ui = new UpdaterUI();

        ui.setStatus("Checking for updates...");
        ui.setVisible(true);

        Thread.sleep(2000);

        double progress = 0;

        while (progress < 1) {
            ui.setStatus(String.format("Downloading updates... (%.1f%%)", progress * 100).replace(".0", ""));
            ui.setProgress(progress);
            progress += .0085;
            Thread.sleep(70);
        }

        ui.setStatus("Installing updates...");
        Thread.sleep(3500);
        ui.setStatus("Done!");
        Thread.sleep(1500);
        ui.close();
    }

}
