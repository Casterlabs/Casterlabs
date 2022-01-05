package co.casterlabs.caffeinated.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.app.AppPreferences;
import co.casterlabs.caffeinated.app.BuildInfo;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.theming.Theme;
import co.casterlabs.caffeinated.app.theming.ThemeManager;
import co.casterlabs.caffeinated.bootstrap.instancing.InstanceManager;
import co.casterlabs.caffeinated.bootstrap.theming.ThemeHandleImpl;
import co.casterlabs.caffeinated.bootstrap.tray.TrayHandler;
import co.casterlabs.caffeinated.bootstrap.ui.ApplicationUI;
import co.casterlabs.caffeinated.bootstrap.ui.UILifeCycleListener;
import co.casterlabs.caffeinated.bootstrap.webview.AppWebview;
import co.casterlabs.caffeinated.localserver.LocalServer;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.Currencies;
import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
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
    public static final String appUrl = "app://app.local";

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
    private static @Getter boolean isDev;
    private static @Getter Bootstrap instance;

    private static AppWebview webview;

    private static LocalServer localServer;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Enable assertions programatically.
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        System.out.println(" > System.out.println(\"Hello World!\");\nHello World!\n\n");

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (!(e instanceof ThreadDeath)) {
                    e.printStackTrace();

                    if (e instanceof UnsatisfiedLinkError) {
                        // TODO show fatal popup detailing the error and let the user know that the
                        // program is about to close.
                    }
                }
            }
        });

        ConsoleUtil.getPlatform(); // Init ConsoleUtil.

//        // This dependency is found in PluginSDK.
//        try {
//            // Mute it's logger.
//            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
//
//            logger.setLevel(Level.WARNING);
//            logger.setUseParentHandlers(false);
//
//            // Enable it.
//            GlobalScreen.registerNativeHook();
//        } catch (Throwable t) {
//            FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst enabling the global keyboard hook.");
//            FastLogger.logException(t);
//        }

        new Thread(() -> {
            new CommandLine(new Bootstrap()).execute(args); // Calls #run()
        }).start();
    }

    @SneakyThrows
    @Override
    public void run() {
        instance = this;

        isDev = this.devAddress != null;
        ReflectionLib.setStaticValue(FileUtil.class, "isDev", isDev);
        buildInfo = Rson.DEFAULT.fromJson(FileUtil.loadResource("build_info.json"), BuildInfo.class);

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
        ReflectionLib.setStaticValue(MusicIntegration.class, "systemPlaybackMusicProvider", NativeSystemProvider.getSystemPlaybackMusicProvider());

        this.registerThemes();
        this.startApp();
    }

    @SuppressWarnings("deprecation")
    private void registerThemes() {
        ThemeManager.setHandle(new ThemeHandleImpl());

        // Light theme
        ThemeManager.registerTheme(
            new Theme("co.casterlabs.light", "Light")
                .withCssFiles(false, "/css/bulma.min.css")
        );

        // Dark theme
        ThemeManager.registerTheme(
            new Theme("co.casterlabs.dark", "Dark")
                .withCssFiles(false, "/css/bulma.min.css", "/css/bulma-prefers-dark.min.css")
                .withClasses("bulma-dark-mode")
                .withDark(true)
        );
    }

    private void startApp() throws Exception {
        CaffeinatedApp app = new CaffeinatedApp(buildInfo, isDev);

        logger.info("Entry                        | Value", buildInfo.getVersionString());
        logger.info("-----------------------------+-------------------------");
        logger.info("buildInfo.versionString      | %s", buildInfo.getVersionString());
        logger.info("buildInfo.author             | %s", buildInfo.getAuthor());
        logger.info("system.platform              | %s", ConsoleUtil.getPlatform().name());
        logger.info("bootstrap.isDev              | %b", isDev);
        logger.info("");

        logger.info("Initializing CEF (it may take some time to extract the natives)");

        // Init and start the local server.
        try {
            localServer = new LocalServer(app.getAppPreferences().get().getConductorPort());

            localServer.start();
        } catch (IOException e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Unable to start LocalServer (conductor):");
            FastLogger.logException(e);
        }

        // Register the custom schemes.
        AppWebview.setSchemeHandler(new ApplicationUI.AppSchemeHandler());

        // Setup the webview.
        webview = AppWebview.getWebviewFactory().produce();

        // Window listeners
        CaffeinatedApp.getInstance().onBridgeEvent("window:move", (json) -> {
            int moveX = json.getNumber("moveX").intValue();
            int moveY = json.getNumber("moveY").intValue();

            ApplicationUI.getWindow().translate(moveX, moveY);
        });

        CaffeinatedApp.getInstance().onBridgeEvent("window:minimize", (json) -> {
            ApplicationUI.getWindow().minimize();
        });

        CaffeinatedApp.getInstance().onBridgeEvent("window:minmax", (json) -> {
            ApplicationUI.getWindow().minmax();
        });

        CaffeinatedApp.getInstance().onBridgeEvent("window:close ", (json) -> {
            ApplicationUI.getWindow().getListener().onUICloseAttempt();
        });

        // Register the lifecycle listener.
        UILifeCycleListener uiLifeCycleListener = new UILifeCycleListener() {

            @Override
            public void onBrowserPreLoad() {
                logger.debug("onPreLoad");

                webview.getJavascriptBridge().setOnEvent((t, d) -> onBridgeEvent(t, d));

                app.init();

                TrayHandler.tryCreateTray();
            }

            @Override
            public void onBrowserInitialLoad() {
                logger.debug("onInitialLoad");
            }

            @Override
            public boolean onUICloseAttempt() {
                logger.debug("onUICloseAttempt");

                if (app.canCloseUI()) {
                    new AsyncTask(() -> {
                        if (CaffeinatedApp.getInstance().getUiPreferences().get().isCloseToTray()) {
                            ApplicationUI.closeWindow();
                        } else {
                            shutdown();
                        }
                    });
                    return true;
                } else {
                    ApplicationUI.focusAndBeep();
                    return false;
                }
            }

            @Override
            public void onMinimize() {
                logger.debug("onMinimize");
                new AsyncTask(() -> {
                    if (CaffeinatedApp.getInstance().getUiPreferences().get().isMinimizeToTray()) {
                        // See if the minimize to tray option is checked.
                        // If so, make sure the app can close before closing the window.
                        if (app.canCloseUI()) {
                            ApplicationUI.closeWindow();
                        } else {
                            ApplicationUI.focusAndBeep();
                        }
                    }
                });
            }

            @Override
            public void onBrowserOpen() {
                logger.debug("onWindowOpen");
            }

            @Override
            public void onBrowserClose() {
                logger.debug("onTrayMinimize");
            }

        };

        webview.setLifeCycleListener(uiLifeCycleListener);

        // Ok, now initialize!
        ApplicationUI.initialize(
            isDev ? this.devAddress : appUrl,
            webview,
            uiLifeCycleListener
        );
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

                case "app:devfeatures": {
                    boolean newValue = data.getBoolean("enabled");

                    PreferenceFile<AppPreferences> prefs = CaffeinatedApp.getInstance().getAppPreferences();
                    prefs.get().setShowDeveloperFeatures(newValue);
                    prefs.save();

                    shutdown(true, true, false);
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
        if (CaffeinatedApp.getInstance().canCloseUI() || force) {
            new AsyncTask(() -> {
                logger.info("Shutting down.");

                // Local Server
                try {
                    localServer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // UI
                TrayHandler.destroy();
                ApplicationUI.getWindow().dispose();

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
            });
        } else {
            ApplicationUI.focusAndBeep();
        }
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
