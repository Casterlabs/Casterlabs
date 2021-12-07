package co.casterlabs.caffeinated.bootstrap;

import java.io.IOException;

import co.casterlabs.caffeinated.app.BuildInfo;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.bootstrap.cef.CefUtil;
import co.casterlabs.caffeinated.bootstrap.instancing.InstanceManager;
import co.casterlabs.caffeinated.bootstrap.tray.TrayHandler;
import co.casterlabs.caffeinated.bootstrap.ui.ApplicationUI;
import co.casterlabs.caffeinated.bootstrap.ui.UILifeCycleListener;
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

@Command(name = "start", mixinStandardHelpOptions = true, version = "Caffeinated", description = "Starts Caffeinated")
public class Bootstrap implements Runnable {
    private static final String appUrl = "app://index.html";

    @Option(names = {
            "-D",
            "--dev-address"
    }, description = "Whether or not this is a dev environment, normal users beware.")
    private String devAddress;

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

    public static void main(String[] args) throws IOException, InterruptedException {
        // Enable assertions programatically.
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        System.out.println(" > System.out.println(\"Hello World!\");\nHello World!\n\n");

        ConsoleUtil.getPlatform(); // Init ConsoleUtil.
        new CommandLine(new Bootstrap()).execute(args); // Calls #run()
    }

    @SneakyThrows
    @Override
    public void run() {
        isDev = this.devAddress != null;
        buildInfo = Rson.DEFAULT.fromJson(FileUtil.loadResource("build_info.json"), BuildInfo.class);

        // Check for another instance, and do IPC things.
        if (!InstanceManager.isSingleInstance()) {
            if (isDev) {
                logger.info("App is already running, closing it now.");
                InstanceManager.closeOtherInstance();
            } else {
                logger.info("App is already running, summoning it now.");

                if (InstanceManager.trySummonInstance()) {
                    FastLoggingFramework.close(); // Faster shutdown.
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

        this.startApp();
    }

    private void startApp() {
        CaffeinatedApp app = new CaffeinatedApp(buildInfo, isDev);

        logger.info("Entry                        | Value", buildInfo.getVersionString());
        logger.info("-----------------------------+-------------------------");
        logger.info("buildInfo.versionString      | %s", buildInfo.getVersionString());
        logger.info("buildInfo.author             | %s", buildInfo.getAuthor());
        logger.info("system.platform              | %s", ConsoleUtil.getPlatform().name());
        logger.info("bootstrap.isDev              | %b", isDev);
        logger.info("");

        logger.info("Initializing CEF (it may take some time to extract the natives)");

        CefUtil.registerSchemes();

        ApplicationUI.initialize(
            isDev ? this.devAddress : appUrl,
            new UILifeCycleListener() {

                @Override
                public void onPreLoad() {
                    logger.debug("onPreLoad");

                    // This is the real meat and potatoes.
                    app.setBridge(ApplicationUI.getBridge());
                    ApplicationUI.getBridge().setOnEvent((t, d) -> onBridgeEvent(t, d));

                    app.init();

                    TrayHandler.tryCreateTray();
                }

                @Override
                public void onInitialLoad() {
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
                public void onWindowOpen() {
                    logger.debug("onWindowOpen");
                }

                @Override
                public void onTrayMinimize() {
                    logger.debug("onTrayMinimize");
                }

            }
        );
    }

    private void onBridgeEvent(String type, JsonObject data) {
        try {
            // Pass it to the app.
            CaffeinatedApp.getInstance().onBridgeEvent(type, data);
        } catch (Throwable t) {
            logger.severe("Uncaught exception whilst processing bridge event:");
            logger.exception(t);
        }
    }

    public static void shutdown() {
        if (CaffeinatedApp.getInstance().canCloseUI()) {
            new AsyncTask(() -> {
                logger.info("Shutting down.");
                TrayHandler.destroy();
                if (ApplicationUI.getDevtools() != null) ApplicationUI.getDevtools().close();
                ApplicationUI.getWindow().dispose();
                CaffeinatedApp.getInstance().shutdown();
                InstanceManager.cleanShutdown();
                FastLoggingFramework.close(); // Faster shutdown.
            });
        } else {
            ApplicationUI.focusAndBeep();
        }
    }

}
