package co.casterlabs.caffeinated.updater;

import java.io.File;

import net.harawata.appdirs.AppDirsFactory;

public class Updater {
    private static final int VERSION = 10;

    private static final String appDataDirectory = AppDirsFactory.getInstance().getUserDataDir("casterlabs-caffeinated", null, null, true);
    private static final File appDirectory = new File(appDataDirectory, "app");
    private static final String javaCommand;

    static {
        appDirectory.mkdirs();

        if (new File("./jre").exists()) {
            javaCommand = "./jre/bin/java";
        } else {
            javaCommand = "java"; // Try the system's install.
        }
    }

}
