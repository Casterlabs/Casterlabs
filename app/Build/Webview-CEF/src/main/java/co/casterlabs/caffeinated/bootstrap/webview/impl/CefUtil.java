package co.casterlabs.caffeinated.bootstrap.webview.impl;

import java.awt.BorderLayout;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings.LogSeverity;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.bootstrap.webview.scheme.SchemeHandler;
import co.casterlabs.caffeinated.util.DualConsumer;
import co.casterlabs.caffeinated.util.Pair;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.EnumProgress;
import me.friwi.jcefmaven.IProgressHandler;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import xyz.e3ndr.consoleutil.consolewindow.BarStyle;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class CefUtil {
    public static final boolean enableOSR = System.getProperty("caffeinated.cef.offscreenrendering.enable", "").equals("true"); // Defaults to false
    public static final boolean enableTransparency = System.getProperty("caffeinated.cef.transparency.enable", "").equals("true"); // Defaults to false

    public static final File bundleDirectory = new File(CaffeinatedApp.appDataDir, "dependencies/cef_bundle");

    public static void create(@NonNull String appScheme, @NonNull SchemeHandler schemeHandler) {
        try {
            CefAppBuilder builder = new CefAppBuilder();

            builder.addJcefArgs("--disable-http-cache", "--disable-web-security");
            builder.setInstallDir(bundleDirectory);

            builder.getCefSettings().windowless_rendering_enabled = enableOSR;
            builder.getCefSettings().log_severity = LogSeverity.LOGSEVERITY_DISABLE;

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

            builder.setProgressHandler(new IProgressHandler() {
                private FastLogger logger = new FastLogger("Cef Bundle");

                private DualConsumer<String, Integer> progressHandler;
                private JFrame progressFrame;

                @Override
                public void handleProgress(EnumProgress state, float percent) {
                    if (percent < 0) {
                        this.logger.info(
                            "%-13s",
                            state
                        );
                    } else {
                        this.logger.info(
                            "%-13s %s",
                            state,
                            BarStyle.ANGLE.format(percent / 100, 30, true)
                        );
                    }

                    if ((state == EnumProgress.DOWNLOADING) && (progressFrame == null)) {
                        Pair<JFrame, DualConsumer<String, Integer>> downloadDialog = createDownloadDialog();

                        this.progressFrame = downloadDialog.a;
                        this.progressHandler = downloadDialog.b;
                    }

                    if (progressFrame != null) {
                        if (state == EnumProgress.INITIALIZED) {
                            this.progressFrame.dispose();
                            this.progressFrame = null;
                            this.progressHandler = null;
                        } else {
                            this.progressHandler.accept(state.name(), (int) percent);
                        }
                    }
                }
            });

            builder.build();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SneakyThrows
    public static CefClient createCefClient() {
        return CefApp.getInstance().createClient();
    }

    @SneakyThrows
    private static Pair<JFrame, DualConsumer<String, Integer>> createDownloadDialog() {
        JFrame frame = new JFrame();

        frame.setLayout(new BorderLayout(0, 0));
        frame.setVisible(true);

        // Setup progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);

        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        frame.getContentPane().add(progressBar, BorderLayout.CENTER);

        // The state text
        JLabel stateText = new JLabel();

        frame.getContentPane().add(stateText, BorderLayout.NORTH);

        // Set the icon.
        {
            URL iconUrl = FileUtil.loadResourceAsUrl("assets/logo/casterlabs.png");
            ImageIcon img = new ImageIcon(iconUrl);

            frame.setIconImage(img.getImage());
        }

        DualConsumer<String, Integer> progressConsumer = (state, progress) -> {
            progressBar.setVisible(progress > 0);
            progressBar.setValue(progress);

            stateText.setText(state);
        };

        return new Pair<>(frame, progressConsumer);
    }

}
