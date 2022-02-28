package co.casterlabs.caffeinated.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.app.BuildInfo;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.bootstrap.impl.NativeBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.linux.common.LinuxBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.macos.common.MacOSBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.WindowsBootstrap;
import co.casterlabs.caffeinated.localserver.LocalServer;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.Currencies;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.util.async.Promise;
import co.casterlabs.kaimen.app.App;
import co.casterlabs.kaimen.app.AppBootstrap;
import co.casterlabs.kaimen.app.AppEntry;
import co.casterlabs.kaimen.app.ui.UIServer;
import co.casterlabs.kaimen.util.platform.Platform;
import co.casterlabs.kaimen.webview.Webview;
import co.casterlabs.kaimen.webview.WebviewFactory;
import co.casterlabs.kaimen.webview.WebviewLifeCycleListener;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@Getter
@Command(name = "start", mixinStandardHelpOptions = true, version = "Caffeinated", description = "Starts Caffeinated")
public class Bootstrap implements Runnable {

    @Option(names = {
            "-D",
            "--dev-address"
    }, description = "Whether or not this is a dev environment, normal users beware.")
    private String devAddress;

    @Option(names = {
            "-dt",
            "--dev-tools"
    }, description = "Enables dev tools.")
    private boolean devToolsEnabled;

    @Option(names = {
            "-d",
            "--debug"
    }, description = "Enables debug logging.")
    private boolean enableDebugLogging;

    @Option(names = {
            "-t",
            "--trace"
    }, description = "Enables trace logging.")
    private boolean enableTraceLogging;

    private static FastLogger logger = new FastLogger();

    private static @Getter BuildInfo buildInfo;
    private static @Getter boolean useAppLoopback;
    private static @Getter boolean isDev;

    private static @Getter Bootstrap instance;
    private static @Getter Webview webview;
    private static LocalServer localServer;
    private static UIServer uiServer;

    // FOR TESTING.
    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        AppBootstrap.main(args);
    }

    @AppEntry
    public static void main() throws Exception {
        App.setName("Casterlabs-Caffeinated");

        System.out.println(" > System.out.println(\"Hello World!\");\nHello World!\n\n");

        NativeBootstrap nb = null;

        switch (Platform.os) {
            case LINUX:
                nb = new LinuxBootstrap();
                break;

            case MACOSX:
                nb = new MacOSBootstrap();
                break;

            case WINDOWS:
                nb = new WindowsBootstrap();
                break;
        }

        assert nb != null : "Unsupported platform: " + Platform.os;

        nb.init();

        new CommandLine(new Bootstrap()).execute(App.getArgs()); // Calls #run()
    }

    @SneakyThrows
    @Override
    public void run() {
        instance = this;

        isDev = this.devAddress != null;
        ReflectionLib.setStaticValue(FileUtil.class, "isDev", isDev);
        buildInfo = Rson.DEFAULT.fromJson(FileUtil.loadResource("build_info.json"), BuildInfo.class);

        writeAppFile("current_build_info.json", FileUtil.loadResourceBytes("build_info.json"));

        ReflectionLib.setStaticValue(CaffeinatedPlugin.class, "devEnvironment", isDev);

        // Check for another instance, and do IPC things.
        if (!InstanceManager.isSingleInstance()) {
            if (isDev) {
                logger.info("App is already running, closing it now.");
                InstanceManager.closeOtherInstance();
                logger.info("Launching as if nothing happened...");
            } else {
                logger.info("App is already running, summoning it now.");

                if (InstanceManager.trySummonInstance()) {
                    return;
                } else {
                    logger.warn("Summon failed, launching anyways.");
                }
            }
        } else {
            logger.info("Starting app.");
        }

        // We do this down here because of the IPC.
        if (this.enableTraceLogging) {
            FastLoggingFramework.setDefaultLevel(LogLevel.TRACE);
        } else if (isDev || this.enableDebugLogging) {
            FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        }

        // Update the log level.
        logger.setCurrentLevel(FastLoggingFramework.getDefaultLevel());

        Currencies.getCurrencies(); // Load the class.

        // Setup the native system
        ReflectionLib.setStaticValue(MusicIntegration.class, "systemPlaybackMusicProvider", NativeSystem.getSystemPlaybackMusicProvider());

        this.startApp();
    }

    private static void writeAppFile(@NonNull String filename, byte[] bytes) throws IOException {
        File file;

        switch (ConsoleUtil.getPlatform()) {
            case MAC:
                if (new File("./").getCanonicalPath().contains(".app")) {
                    file = new File("../../../", filename);
                    break;
                }
                // Otherwise, break free.

            case UNIX:
            case WINDOWS:
            case UNKNOWN:
            default:
                file = new File(filename);
                break;
        }

        if (bytes == null) {
            file.createNewFile();
        } else {
            Files.write(file.toPath(), bytes);
        }
    }

    private void startApp() throws Exception {
        CaffeinatedApp app = new CaffeinatedApp(buildInfo, isDev);

        logger.info("Entry                        | Value", buildInfo.getVersionString());
        logger.info("-----------------------------+-------------------------");
        logger.info("buildInfo.versionString      | %s", buildInfo.getVersionString());
        logger.info("buildInfo.author             | %s", buildInfo.getAuthor());
        logger.info("buildInfo.isDev              | %b", isDev);
        logger.info("system.platform              | %s", ConsoleUtil.getPlatform().name());
        logger.info("bootstrap.args               | %s", System.getProperty("sun.java.command"));
        logger.info("");

        // Init and start the local server.
        try {
            localServer = new LocalServer(app.getAppPreferences().get().getConductorPort());

            localServer.start();
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Unable to start LocalServer (conductor):");
            FastLogger.logException(e);
        }

        uiServer = new UIServer();
        uiServer.setHandler(new AppSchemeHandler());
        uiServer.start();

        String appUrl = isDev ? this.devAddress : uiServer.getAddress();

        // Setup the webview.
        logger.info("Initializing UI (this may take some time)");
        webview = WebviewFactory.get().produce();

        // Register the lifecycle listener.
        WebviewLifeCycleListener uiLifeCycleListener = new WebviewLifeCycleListener() {

            @Override
            public void onBrowserPreLoad() {
                logger.debug("onPreLoad");

                webview.getBridge().setOnEvent((t, d) -> onBridgeEvent(t, d));

                app.setAppBridge(webview.getBridge());
                app.setWebview(webview);
                app.setAppUrl(appUrl);
                app.init();

                try {
                    new Promise<>(() -> {
                        TrayHandler.tryCreateTray(webview);
                        return null;
                    }).await();
                } catch (Throwable ignored) {}
            }

            @Override
            public void onMinimize() {
                logger.debug("onMinimize");
                new AsyncTask(() -> {
                    if (CaffeinatedApp.getInstance().getUiPreferences().get().isMinimizeToTray()) {
                        // See if the minimize to tray option is checked.
                        // If so, make sure the app can close before closing the window.
                        if (app.canCloseUI()) {
                            webview.close();
                        } else {
                            webview.focus();
                        }
                    }
                });
            }

            @Override
            public void onBrowserOpen() {
                logger.debug("onWindowOpen");
                TrayHandler.updateShowCheckbox(true);
            }

            @Override
            public void onBrowserClose() {
                logger.debug("onBrowserClose");
                TrayHandler.updateShowCheckbox(false);
            }

            @Override
            public void onOpenRequested() {
                logger.debug("onOpenRequested");
                webview.open(appUrl);
            }

            @Override
            public void onCloseRequested() {
                logger.debug("onCloseRequested");

                if (app.canCloseUI()) {
                    new AsyncTask(() -> {
                        if (CaffeinatedApp.getInstance().getUiPreferences().get().isCloseToTray()) {
                            webview.close();
                        } else {
                            shutdown();
                        }
                    });
                } else {
                    webview.focus();
                }
            }

        };

        logger.info("Starting the UI");
        webview.initialize(
            uiLifeCycleListener,
            app.getWindowPreferences().get(),
            false,
            false
        );

        logger.info("appAddress = %s", appUrl);
        webview.open(appUrl);

        // If all of that succeeds, we write a file to let the updater know that
        // everything's okay.
        writeAppFile(".build_ok", null);
    }

    private void onBridgeEvent(String type, JsonObject data) {
        try {
            switch (type) {
                case "debug:gc": {
                    System.gc();
                    return;
                }

                case "app:reset": {
                    shutdown(true, true, true);
                    return;
                }

                case "ui:theme-loaded": {
                    new AsyncTask(() -> {
//                        JFrame frame = ApplicationUI.getWindow().getFrame();

                        // If we enable osr, we want to only open the window when it's fully loaded.
//                        if (webview.isOffScreenRenderingEnabled()) {
//                            frame.setVisible(true);
//                            frame.toFront();
//                        }

                        // We also want to wait a bit for the app to initialize further (and to make the
                        // ux less jarring since it loads too fast.)
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException ignored) {}

                        // Forward the event, after the timeout.
                        CaffeinatedApp.getInstance().onBridgeEvent(type, data);
                    });
                    return;
                }

                default: {
                    // Pass it to the app.
                    CaffeinatedApp.getInstance().onBridgeEvent(type, data);
                    return;
                }

            }
        } catch (Throwable t) {
            logger.severe("Uncaught exception whilst processing bridge event:");
            logger.exception(t);
        }
    }

    public static void shutdown() {
        shutdown(false, false, false);
    }

    private static void shutdown(boolean force, boolean relaunch, boolean isReset) {
        new AsyncTask(() -> {
            if (CaffeinatedApp.getInstance().canCloseUI() || force) {
                logger.info("Shutting down.");

                // Local Server
                try {
                    localServer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // UI
                TrayHandler.destroy();
                webview.destroy();

                try {
                    uiServer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // App
                CaffeinatedApp.getInstance().shutdown();
                InstanceManager.cleanShutdown();

                // Exit.
                if (isReset) {
                    try {
                        Files.walk(new File(CaffeinatedApp.appDataDir).toPath())
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (relaunch) {
                    relaunch();
                } else {
                    System.exit(0);
                }
            } else {
                webview.focus();
            }
        });
    }

    @SneakyThrows
    private static void relaunch() {
        String jvmArgs = String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments());
        String entry = System.getProperty("sun.java.command"); // Tested, present in OpenJDK and Oracle
        String classpath = System.getProperty("java.class.path");
        String javaHome = System.getProperty("java.home");

        String[] args = entry.split(" ");
        File entryFile = new File(args[0]);

        if (entryFile.exists()) { // If the entry is a file, not a main method.
            args[0] = '"' + entryFile.getCanonicalPath() + '"'; // Use raw file path.

            Runtime.getRuntime().exec(String.format("\"%s/bin/java\" %s -cp \"%s\" -jar %s", javaHome, jvmArgs, classpath, String.join(" ", args)));
        } else {
            Runtime.getRuntime().exec(String.format("\"%s/bin/java\" %s -cp \"%s\" %s", javaHome, jvmArgs, classpath, entry));
        }

        FastLogger.logStatic("Relaunching!");
        System.exit(0);
    }

}