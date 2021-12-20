package co.casterlabs.caffeinated.bootstrap.cef;

import java.io.File;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings.LogSeverity;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;

import co.casterlabs.caffeinated.bootstrap.cef.scheme.SchemeHandler;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.impl.ResponseResourceHandler;
import co.casterlabs.caffeinated.bootstrap.ui.ApplicationUI.AppSchemeHandler;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.EnumProgress;
import me.friwi.jcefmaven.IProgressHandler;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import xyz.e3ndr.consoleutil.consolewindow.BarStyle;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class CefUtil {
    public static final boolean enableOSR = false;
    public static final boolean enableTransparency = false;

    static {
        try {
            CefAppBuilder builder = new CefAppBuilder();

            builder.addJcefArgs("--disable-http-cache", "--disable-web-security");
            builder.setInstallDir(new File("./cef_bundle"));

            builder.getCefSettings().windowless_rendering_enabled = enableOSR;
            builder.getCefSettings().log_severity = LogSeverity.LOGSEVERITY_DISABLE;

            builder.setAppHandler(new MavenCefAppHandlerAdapter() {

                @Override
                public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
                    registrar.addCustomScheme(
                        "app",
                        true, // isStandard
                        false, // isLocal
                        false, // isDisplayIsolated
                        true,  // isSecure
                        true,  // isCorsEnabled
                        true,  // isCspBypassing
                        true   // isFetchEnabled
                    );

                    registrar.addCustomScheme(
                        "proxy",
                        true,  // isStandard
                        false, // isLocal
                        false, // isDisplayIsolated
                        true,  // isSecure
                        true,  // isCorsEnabled
                        true,  // isCspBypassing
                        true   // isFetchEnabled
                    );
                }

                @Override
                public void onContextInitialized() {
                    CefUtil.registerUrlScheme("app", new AppSchemeHandler());
                    CefUtil.registerUrlScheme("proxy", new CorsProxySchemeHandler());
                }

            });

            builder.setProgressHandler(new IProgressHandler() {
                private FastLogger logger = new FastLogger("Cef Bundle");

                @Override
                public void handleProgress(EnumProgress state, float percent) {
                    percent = Math.max(0, percent) / 100;

                    this.logger.info(
                        "%-13s %s",
                        state,
                        BarStyle.ANGLE.format(percent, 30, true)
                    );
                }
            });

            builder.build();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void registerSchemes() {
        // This is a blank method that'll automatically call static {} once.
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
