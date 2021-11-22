package co.casterlabs.caffeinated.util;

import java.io.IOException;

import lombok.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebUtil {
    private static final OkHttpClient client = new OkHttpClient();

    public static String sendHttpRequest(@NonNull Request.Builder builder) throws IOException {
        try (Response response = client.newCall(builder.build()).execute()) {
            String body = response.body().string();

//            FastLogger.logStatic(LogLevel.TRACE, "%s: %d\n%s\n", builder.getUrl$okhttp(), response.code(), body);

            return body;
        }
    }

}
