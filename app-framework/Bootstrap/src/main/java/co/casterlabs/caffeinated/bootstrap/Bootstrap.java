package co.casterlabs.caffeinated.bootstrap;

import java.io.IOException;

import co.casterlabs.caffeinated.bootstrap.ui.ApplicationUI;
import co.casterlabs.caffeinated.bootstrap.ui.UILifeCycleListener;
import co.casterlabs.rakurai.json.Rson;
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

        buildInfo = Rson.DEFAULT.fromJson(FileUtil.loadResource("build_info.json"), BuildInfo.class);

        System.out.println("\n > System.out.println(\"Hello World!\");\nHello World!\n\n");

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

                }

                @Override
                public void onInitialLoad() {
                    logger.debug("onInitialLoad");

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

}
