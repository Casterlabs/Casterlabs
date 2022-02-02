package co.casterlabs.caffeinated.webview.impl;

import java.io.File;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.CefSettings.LogSeverity;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;

import co.casterlabs.caffeinated.webview.Webview;
import co.casterlabs.caffeinated.webview.scheme.SchemeHandler;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;

public class CefUtil {
    public static final File bundleDirectory = new File("dependencies/cef_bundle");

    public static void create(boolean enableOsr, @NonNull String appScheme, @NonNull SchemeHandler schemeHandler) {
        try {
            CefAppBuilder builder = new CefAppBuilder();

            builder.addJcefArgs("--disable-http-cache", "--disable-web-security");
            builder.setInstallDir(bundleDirectory);

            CefSettings settings = builder.getCefSettings();

            settings.windowless_rendering_enabled = enableOsr;
            settings.log_severity = LogSeverity.LOGSEVERITY_DISABLE;
            settings.user_agent_product = String.format("Chromium; Just A CasterlabsCaffeinated (%s)", Webview.STATE_PASSWORD);

            builder.setAppHandler(new MavenCefAppHandlerAdapter() {

                @Override
                public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
                    registrar.addCustomScheme(
                        appScheme,
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
                    CefApp
                        .getInstance()
                        .registerSchemeHandlerFactory(appScheme, "", new CefSchemeHandlerFactory() {
                            @Override
                            public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
                                if (schemeName.equals(appScheme)) {
                                    return new CefResponseResourceHandler(schemeHandler);
                                }

                                return null;
                            }
                        });
                }

            });

            builder.setProgressHandler(new CefDownloadProgressDialog());

            builder.build();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SneakyThrows
    public static CefClient createCefClient() {
        return CefApp.getInstance().createClient();
    }

}
