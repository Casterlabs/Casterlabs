package co.casterlabs.caffeinated.bootstrap.cef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.SchemeHandler;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.HttpMethod;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.HttpRequest;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.HttpResponse;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.StandardHttpStatus;
import co.casterlabs.caffeinated.bootstrap.ui.ApplicationUI;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class CorsProxySchemeHandler implements SchemeHandler {
    private static final OkHttpClient client = new OkHttpClient();
    private static String ALLOWED_METHODS;

    static {
        List<String> methods = new ArrayList<>();
        for (HttpMethod method : HttpMethod.values()) {
            methods.add(method.name());
        }

        ALLOWED_METHODS = String.join(", ", methods);
    }

    @Override
    public HttpResponse onRequest(HttpRequest request) {
        String[] url = request.getUri().substring("proxy://".length()).split("/", 2);
        String host = url[0];
        String uri = (url.length > 1) ? ('/' + url[1]) : "";

        Request.Builder builder = new Request.Builder()
            .url("https://" + host + uri);

        if (request.hasBody()) {
            try {
                FastLogger.logStatic(request.getRequestBody());
                builder.method(request.getMethod().name(), RequestBody.create(request.getRequestBodyBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (Response response = client.newCall(builder.build()).execute()) {
            HttpResponse result = HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, response.body().bytes());

            response
                .headers()
                .forEach((header) -> {
                    result.putHeader(header.getFirst(), header.getSecond());
                });

            // Enable CORS in the dev environment.
            String origin = "null";
            if (Bootstrap.isDev()) {
                String[] split = ApplicationUI.getAppAddress().split("://");
                String protocol = split[0];
                String referer = split[1].split("/")[0]; // Strip protocol and uri

                origin = protocol + "://" + referer;
            }

            result.putHeader("Access-Control-Allow-Origin", origin);
            result.putHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
            result.putHeader("Access-Control-Allow-Headers", "Authorization, *");

            result.putHeader("server", "Casterlabs-Caffeinated (CaffeineApiProxy)");

            return result;
        } catch (IOException e) {
            FastLogger.logException(e);
            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.INTERNAL_ERROR, "Internal Error (Caffeinated)")
                .setMimeType("text/plain");
        }
    }

}
