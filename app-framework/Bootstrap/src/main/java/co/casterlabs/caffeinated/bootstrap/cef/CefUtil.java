package co.casterlabs.caffeinated.bootstrap.cef;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.CefSettings.LogSeverity;
import org.cef.JCefLoader;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCommandLine;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefAppHandler;
import org.cef.handler.CefPrintHandler;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;

import co.casterlabs.caffeinated.bootstrap.cef.scheme.SchemeHandler;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.impl.ResponseResourceHandler;
import co.casterlabs.caffeinated.bootstrap.ui.ApplicationUI.AppSchemeHandler;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class CefUtil {

    static {
        try {
            CefApp app = JCefLoader.installAndLoadCef("--disable-http-cache");

            // The default settings has broken defaults.
            CefSettings settings = ReflectionLib.getValue(app, "settings_");

            settings.log_severity = LogSeverity.LOGSEVERITY_DISABLE;

            app.setSettings(settings);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void registerSchemes() {
        // This had to be moved before the first createClient call.
        CefApp.addAppHandler(new CefAppHandler() {

            @Override
            public void stateHasChanged(CefAppState var1) {}

            @Override
            public void onScheduleMessagePumpWork(long delay_ms) {
                // For some reason the default implementation is not smart enough to do this on
                // it's own. So we manually do it.
                CefApp.getInstance().doMessageLoopWork(delay_ms);
            }

            @Override
            public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
                if (!registrar.addCustomScheme(
                    "app", // Scheme
                    true, // isStandard
                    false, // isLocal
                    false, // isDisplayIsolated
                    true, // isSecure
                    false, // isCorsEnabled
                    false, // isCspBypassing
                    true // isFetchEnabled
                )) {
                    FastLogger.logStatic(LogLevel.SEVERE, "Could not register scheme.");
                    System.exit(1);
                }
            }

            @Override
            public void onContextInitialized() {
                CefUtil.registerUrlScheme("app", new AppSchemeHandler());
            }

            @Override
            public boolean onBeforeTerminate() {
                return false;
            }

            @Override
            public void onBeforeCommandLineProcessing(String var1, CefCommandLine var2) {}

            @Override
            public CefPrintHandler getPrintHandler() {
                return null;
            }

        });
    }

    @SneakyThrows
    public static CefClient createCefClient() {
        return CefApp.getInstance().createClient();
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
