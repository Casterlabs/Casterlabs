package co.casterlabs.caffeinated.cef;

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

public class CefUtil {

    public static Pandomium createCefApp() {
        // We use Pandomium to bundle the CEF natives and load them,
        // thanks Pandominum team! <3
        Pandomium panda = Pandomium.builder()
            .build();

        panda.getLogger().setThreshold(Channel.FATAL);

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
