package co.casterlabs.caffeinated.bootstrap;

import java.io.IOException;

import co.casterlabs.caffeinated.app.BuildInfo;
import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.bootstrap.events.window.AppWindowEventType;
import co.casterlabs.caffeinated.bootstrap.events.window.AppWindowThemeLoadedEvent;
import co.casterlabs.caffeinated.bootstrap.ui.ApplicationUI;
import co.casterlabs.caffeinated.bootstrap.ui.UILifeCycleListener;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.events.AbstractEvent;
import xyz.e3ndr.eventapi.listeners.EventListener;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@Command(name = "start", mixinStandardHelpOptions = true, version = "Caffeinated", description = "Starts Caffeinated")
public class Bootstrap implements Runnable {

    @Option(names = {
            "-D",
            "--dev-address"
    }, description = "Whether or not this is a dev environment, normal users beware.")
    private String devAddress;

    @Option(names = {
            "-t",
            "--trace"
    }, description = "Enables Trace Logging.")
    private boolean enableTraceLogging;

    private static FastLogger logger = new FastLogger();

    private static @Getter BuildInfo buildInfo;
    private static @Getter boolean isDev;
    private static @Getter CaffeinatedApp app;

    private EventHandler<AppWindowEventType> handler = new EventHandler<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        ConsoleUtil.getPlatform(); // Init ConsoleUtil.
        new CommandLine(new Bootstrap()).execute(args); // Calls #run()
    }

    @SneakyThrows
    @Override
    public void run() {
        isDev = this.devAddress != null;

        if (this.enableTraceLogging) {
            FastLoggingFramework.setDefaultLevel(LogLevel.TRACE);
        } else if (isDev) {
            FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        }

        this.handler.register(this);

        app = new CaffeinatedApp(buildInfo, isDev);

        buildInfo = Rson.DEFAULT.fromJson(FileUtil.loadResource("build_info.json"), BuildInfo.class);

        System.out.println(" > System.out.println(\"Hello World!\");\nHello World!\n\n");

        logger.info("Entry                        | Value", buildInfo.getVersionString());
        logger.info("-----------------------------+-------------------------");
        logger.info("buildInfo.versionString      | %s", buildInfo.getVersionString());
        logger.info("buildInfo.author             | %s", buildInfo.getAuthor());
        logger.info("system.platform              | %s", ConsoleUtil.getPlatform().name());
        logger.info("bootstrap.isDev              | %b", isDev);

        logger.info("Initializing CEF (it may take some time to download the natives)");

        ApplicationUI.initialize(
            isDev ? this.devAddress : "app://index",
            new UILifeCycleListener() {

                @Override
                public void onPreLoad() {
                    logger.debug("onPreLoad");

                    // This is the real meat and potatoes.
                    app.setBridge(ApplicationUI.getBridge());

                    ApplicationUI.getBridge().setOnEvent((t, d) -> onBridgeEvent(t, d));
                }

                @Override
                public void onInitialLoad() {
                    logger.debug("onInitialLoad");
                    app.init();
                }

                @Override
                public boolean onCloseAttempt() {
                    logger.debug("onCloseAttempt");
                    return true;
                }

                @Override
                public void onTrayMinimize() {
                    logger.debug("onTrayMinimize");
                }

            }
        );
    }

//    @EventListener
//    public void onAppWindowMinimizeEvent(AppWindowMinimizeEvent event) {
//        ApplicationUI.getWindow().getFrame().setState(JFrame.ICONIFIED);
//    }
//
//    @EventListener
//    public void onAppWindowMinMaxEvent(AppWindowMinMaxEvent event) {
//        if ((ApplicationUI.getWindow().getFrame().getState() & JFrame.MAXIMIZED_BOTH) != 0) {
//            ApplicationUI.getWindow().getFrame().setState(JFrame.MAXIMIZED_BOTH);
//        } else {
//            ApplicationUI.getWindow().getFrame().setState(JFrame.NORMAL);
//        }
//    }
//
//    @EventListener
//    public void onAppWindowCloseEvent(AppWindowCloseEvent event) {
//        ApplicationUI.getWindow().tryClose();
//    }

    @EventListener
    public void onAppWindowThemeLoadedEvent(AppWindowThemeLoadedEvent event) {}

    @SneakyThrows
    private void onBridgeEvent(String type, JsonObject data) {
        if (type.startsWith("window")) {
            String signal = type.split(":", 2)[1].replace('-', '_').toUpperCase();

            AppWindowEventType eventType = AppWindowEventType.valueOf(signal);
            AbstractEvent<AppWindowEventType> event = Rson.DEFAULT.fromJson(data, eventType.getEventClass());

            this.handler.call(event);
        } else {
            // Pass it to the app.
            app.onBridgeEvent(type, data);
        }
    }

}
