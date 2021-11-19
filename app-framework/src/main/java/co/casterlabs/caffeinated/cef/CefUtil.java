package co.casterlabs.caffeinated.cef;

import java.io.PrintStream;

import org.cef.CefApp;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;
import org.panda_lang.pandomium.Pandomium;

import co.casterlabs.caffeinated.cef.scheme.SchemeHandler;
import co.casterlabs.caffeinated.cef.scheme.impl.ResponseResourceHandler;
import lombok.NonNull;
import net.dzikoysk.dynamiclogger.Channel;
import net.dzikoysk.dynamiclogger.Logger;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.fastloggingframework.logging.LoggingUtil;

public class CefUtil {

    private static LogLevel channelToLevel(Channel ch) {
        switch ((int) ch.getPriority()) {

            case 0:
            case 10: {
                return LogLevel.TRACE;
            }

            case 20: {
                return LogLevel.DEBUG;
            }

            case 30: {
                return LogLevel.INFO;
            }

            case 40: {
                return LogLevel.WARNING;
            }

            case 50:
            case 60: {
                return LogLevel.SEVERE;
            }

            default: {
                return LogLevel.NONE;
            }

        }
    }

    public static Pandomium createCefApp() {
        // We use Pandomium to bundle the CEF natives and load them,
        // thanks Pandominum team! <3
        FastLogger pandaLogger = new FastLogger("CefLoader/Pandomium");
        Pandomium panda = Pandomium.builder()
            .logger(new Logger() {
                @Override
                public Logger getLogger() {
                    return this;
                }

                @Override
                public Logger log(Channel channel, Object message, Object... arguments) {
                    LogLevel level = channelToLevel(channel);
                    if ((level == LogLevel.WARNING) || (level == LogLevel.SEVERE)) {
                        pandaLogger.log(level, LoggingUtil.parseFormat(message, arguments));
                    }
                    return this;
                }

                @Override
                public Logger exception(Channel channel, Throwable throwable) {
                    pandaLogger.exception(throwable);
                    return this;
                }

                @Override
                public Logger setThreshold(Channel threshold) {
                    pandaLogger.setCurrentLevel(channelToLevel(threshold));
                    return this;
                }

                @Override
                public PrintStream toPrintStream(Channel channel) {
                    return null;
                }
            })
            .build();

        panda.getCommandLine().addArgument("--log-severity=disable");

        return panda;
    }

    public static void registerUrlScheme(@NonNull String scheme, @NonNull SchemeHandler handler) {
        CefApp app = CefApp.getInstance();

        app.registerSchemeHandlerFactory(scheme, "", new CefSchemeHandlerFactory() {
            @Override
            public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
                if (schemeName.equals(scheme)) {
                    return new ResponseResourceHandler(handler);
                }

                return null;
            }
        });
    }

}
