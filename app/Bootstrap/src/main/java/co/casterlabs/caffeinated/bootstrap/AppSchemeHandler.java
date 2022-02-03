package co.casterlabs.caffeinated.bootstrap;

import java.io.IOException;

import co.casterlabs.caffeinated.webview.scheme.SchemeHandler;
import co.casterlabs.caffeinated.webview.scheme.http.HttpRequest;
import co.casterlabs.caffeinated.webview.scheme.http.HttpResponse;
import co.casterlabs.caffeinated.webview.scheme.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.MimeTypes;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AppSchemeHandler implements SchemeHandler {

    @Override
    public HttpResponse onRequest(HttpRequest request) {
        String uri = request.getUri().substring(Bootstrap.getAppUrl().length());

        // Append `index.html` to the end when required.
        if (!uri.contains(".")) {
            if (uri.endsWith("/")) {
                uri += "index.html";
            } else {
                uri += "/index.html";
            }
        }

        try {
            byte[] content = FileUtil.loadResourceBytes("app" + uri);
            String mimeType = "application/octet-stream";

            String[] split = uri.split("\\.");
            if (split.length > 1) {
                mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
            }

            FastLogger.logStatic(LogLevel.DEBUG, "200 %s -> app%s (%s)", request.getUri(), uri, mimeType);

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                .setMimeType(mimeType);
        } catch (IOException e) {
            FastLogger.logStatic(LogLevel.SEVERE, "404 %s -> app%s", request.getUri(), uri);

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.NOT_FOUND, "")
                .setMimeType("application/octet-stream");
        }
    }

}
