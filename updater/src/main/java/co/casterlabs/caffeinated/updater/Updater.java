package co.casterlabs.caffeinated.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.util.WebUtil;
import co.casterlabs.caffeinated.updater.util.ZipUtil;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.rakurai.io.IOUtil;
import lombok.Getter;
import net.harawata.appdirs.AppDirsFactory;
import okhttp3.Request;
import okhttp3.Response;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Updater {
    private static final int VERSION = 10;

    // TODO replace "beta" with "stable" once the app is ready.
    private static String REMOTE_ZIP_DOWNLOAD_URL = "https://cdn.casterlabs.co/dist/beta/";
    private static final String REMOTE_COMMIT_URL = "https://cdn.casterlabs.co/dist/beta/commit.txt";
    private static final String LAUNCHER_VERSION_URL = "https://cdn.casterlabs.co/dist/beta/updater-version.txt";

    public static final String appDataDirectory = AppDirsFactory.getInstance().getUserDataDir("casterlabs-caffeinated", null, null, true);
    private static final File appDirectory = new File(appDataDirectory, "app");
    private static final File updateFile = new File(appDirectory, "update.zip");
    private static final File commitFile = new File(appDirectory, "commit.txt");
    private static final File buildokFile = new File(appDirectory, ".build_ok");

    private static @Getter boolean isLauncherOutOfDate = false;
    private static @Getter boolean isPlatformSupported = true;

    private static String launchCommand = appDirectory + "/Casterlabs-Caffeinated";

    static {
        appDirectory.mkdirs();

        switch (ConsoleUtil.getPlatform()) {
            case MAC:
                REMOTE_ZIP_DOWNLOAD_URL += "caffeinated-macos.zip";
                break;

            case UNIX:
                REMOTE_ZIP_DOWNLOAD_URL += "caffeinated-linux.zip";
                break;

            case WINDOWS:
                launchCommand += ".exe";
                REMOTE_ZIP_DOWNLOAD_URL += "caffeinated-windows.zip";
                break;

            case UNKNOWN:
                isPlatformSupported = false;
                break;

        }

        try {
            int remoteLauncherVersion = Integer.parseInt(WebUtil.sendHttpRequest(new Request.Builder().url(LAUNCHER_VERSION_URL)).trim());

            isLauncherOutOfDate = VERSION < remoteLauncherVersion;
        } catch (Exception e) {
            FastLogger.logException(e);
        }
    }

    public static void borkInstall() {
        buildokFile.delete();
    }

    public static boolean needsUpdate() {
        try {
            // Check for existence of files.
            if (!commitFile.exists()) {
                return true;
            } else if (!buildokFile.exists()) {
                FastLogger.logStatic("Build was not healthy, forcing redownload.");
                return true;
            } else {
                // Check the version.
                String installedCommit = FileUtil.readFile(commitFile).trim();
                String remoteCommit = WebUtil.sendHttpRequest(new Request.Builder().url(REMOTE_COMMIT_URL)).trim();

                return !remoteCommit.equals(installedCommit);
            }
        } catch (IOException e) {
            FastLogger.logException(e);
            return true;
        }
    }

    public static void downloadAndInstallUpdate(UpdaterDialog dialog) throws UpdaterException {
        FileUtil.emptyDirectory(appDirectory);

        try (Response response = WebUtil.sendRawHttpRequest(new Request.Builder().url(REMOTE_ZIP_DOWNLOAD_URL))) {
            // Download zip.
            {
                dialog.setStatus("Downloading updates...");

                InputStream source = response.body().byteStream();
                OutputStream dest = new FileOutputStream(updateFile);

                double totalSize = response.body().contentLength();
                int totalRead = 0;

                byte[] buffer = new byte[IOUtil.DEFAULT_BUFFER_SIZE];
                int read = 0;

                while ((read = source.read(buffer)) != -1) {
                    dest.write(buffer, 0, read);
                    totalRead += read;

                    double progress = totalRead / totalSize;

                    dialog.setStatus(String.format("Downloading updates... (%.0f%%)", progress * 100));
                    dialog.setProgress(progress);
                }

                dest.flush();

                source.close();
                dest.close();

                dialog.setProgress(-1);
            }

            // Extract zip
            {
                dialog.setStatus("Installing updates...");
                ZipUtil.unzip(updateFile, appDirectory);

                updateFile.delete();
            }
        } catch (Exception e) {
            throw new UpdaterException(UpdaterException.Error.DOWNLOAD_FAILED, "Update failed :(", e);
        }
    }

    public static void launch() throws UpdaterException {
        try {
            // TODO wait for .build_ok to show up.
            // (We will need to start bundling cef rather than having it downloaded)

            new ProcessBuilder()
                .directory(appDirectory)
                .command(launchCommand)
                .start();

            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            throw new UpdaterException(UpdaterException.Error.LAUNCH_FAILED, "Could not launch update :(", e);
        }
    }

}
