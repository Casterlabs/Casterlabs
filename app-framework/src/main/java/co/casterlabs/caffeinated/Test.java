package co.casterlabs.caffeinated;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefCommandLine;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefAppHandler;
import org.cef.handler.CefPrintHandler;
import org.panda_lang.pandomium.Pandomium;
import org.panda_lang.pandomium.wrapper.PandomiumClient;

import co.casterlabs.caffeinated.cef.CefUtil;
import co.casterlabs.caffeinated.cef.scheme.SchemeHandler;
import co.casterlabs.caffeinated.cef.scheme.http.HttpRequest;
import co.casterlabs.caffeinated.cef.scheme.http.HttpResponse;
import co.casterlabs.caffeinated.cef.scheme.http.StandardHttpStatus;
import lombok.SneakyThrows;

public class Test {

    @SneakyThrows
    public static void main(String[] args) {
        registerSchemes();

        Pandomium panda = CefUtil.createCefApp();
        PandomiumClient client = panda.createClient();

//        JavascriptBridge bridge = new JavascriptBridge(client.getCefClient());

        CefBrowser browser = client.loadURL("app://index");

        JFrame frame = new JFrame();
        frame.getContentPane().add(browser.getUIComponent(), BorderLayout.CENTER);

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
            }
        });

        frame.setTitle("Casterlabs-Caffeinated");
        frame.setSize(800, 600);
        frame.setVisible(true);

        Thread.sleep(5000);

//        bridge.emit("test!", new JsonObject());
    }

    public static void registerSchemes() {
        CefApp.addAppHandler(new CefAppHandler() {

            @Override
            public void stateHasChanged(CefAppState var1) {}

            @Override
            public void onScheduleMessagePumpWork(long var1) {}

            @Override
            public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
                if (!registrar.addCustomScheme(
                    "app", // Scheme
                    true,  // isStandard
                    false, // isLocal
                    false, // isDisplayIsolated
                    true,  // isSecure
                    false, // isCorsEnabled
                    false, // isCspBypassing
                    true   // isFetchEnabled
                )) {
                    System.out.println("Could not register scheme.");
                }
            }

            @Override
            public void onContextInitialized() {
                CefUtil.registerUrlScheme("app", new TestSchemeHandler());
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

    public static class TestSchemeHandler implements SchemeHandler {

        @Override
        public HttpResponse onRequest(HttpRequest request) {
            String content = "<!DOCTYPE html>"
                + "<html>"
                + "<meta charset=\"utf-8\">"
                + "<body>"
                + "Hello World!"
                + "<br />"
                + "<br />"
                + "<br />"
                + "to: world 🌎"
                + "<br />"
                + "from: Casterlabs-Caffeinated (JCEF/Pandomium)"
                + "</body>"
                + "</html>";

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                .setMimeType("text/html");
        }

    }

}
