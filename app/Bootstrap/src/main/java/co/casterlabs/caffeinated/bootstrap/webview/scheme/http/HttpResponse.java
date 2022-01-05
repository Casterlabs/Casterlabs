package co.casterlabs.caffeinated.bootstrap.webview.scheme.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class HttpResponse {
    public static final HttpResponse INTERNAL_ERROR = HttpResponse.newFixedLengthResponse(StandardHttpStatus.INTERNAL_ERROR, new byte[0]);
    public static final byte[] EMPTY_BODY = new byte[0];

    private @Getter(AccessLevel.NONE) Map<String, String> headers = new HashMap<>();
    private @NonNull @Setter HttpStatus status;

    private InputStream responseStream;
    private TransferEncoding mode;
    private int length = -1;

    private HttpResponse(InputStream responseStream, TransferEncoding mode, HttpStatus status) {
        this.responseStream = responseStream;
        this.mode = mode;
        this.status = status;
    }

    /* ---------------- */
    /* Headers          */
    /* ---------------- */

    public HttpResponse setMimeType(String type) {
        return this.putHeader("content-type", type);
    }

    public HttpResponse putHeader(@NonNull String key, @NonNull String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpResponse putAllHeaders(@NonNull Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this.headers.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public boolean hasHeader(@NonNull String key) {
        return this.headers.containsKey(key);
    }

    public boolean removeHeader(@NonNull String key) {
        return this.headers.remove(key) != null;
    }

    public Map<String, String> getAllHeaders() {
        return this.headers;
    }

    /* ---------------- */
    /* Creating         */
    /* ---------------- */

    public static HttpResponse newFixedLengthResponse(@NonNull HttpStatus status) {
        return newFixedLengthResponse(status, EMPTY_BODY);
    }

    public static HttpResponse newFixedLengthResponse(@NonNull HttpStatus status, @NonNull String body) {
        return newFixedLengthResponse(status, body.getBytes(StandardCharsets.UTF_8));
    }

    public static HttpResponse newFixedLengthResponse(@NonNull HttpStatus status, @NonNull JsonElement json) {
        if ((json instanceof JsonObject) || (json instanceof JsonArray)) {
            byte[] body = json
                .toString(false)
                .getBytes(StandardCharsets.UTF_8);

            return newFixedLengthResponse(status, body)
                .setMimeType("application/json");
        } else {
            throw new IllegalArgumentException("Json must be an Object or Array.");
        }
    }

    public static HttpResponse newFixedLengthResponse(@NonNull HttpStatus status, @NonNull byte[] body) {
        return newFixedLengthResponse(status, new ByteArrayInputStream(body), body.length);
    }

    public static HttpResponse newFixedLengthResponse(@NonNull HttpStatus status, @NonNull InputStream responseStream, int length) {
        if (length > Integer.MAX_VALUE) {
            return newChunkedResponse(status, responseStream);
        } else {
            HttpResponse response = new HttpResponse(responseStream, TransferEncoding.FIXED_LENGTH, status);

            response.length = length;

            return response;
        }
    }

    public static HttpResponse newChunkedFileResponse(@NonNull HttpStatus status, @NonNull File file) throws FileNotFoundException {
        FileInputStream fin = new FileInputStream(file);

        return newChunkedResponse(status, fin);
    }

    public static HttpResponse newChunkedFileResponse(@NonNull HttpStatus status, @NonNull File file, long skip, long length) throws FileNotFoundException, IOException {
        FileInputStream fin = new FileInputStream(file);

        fin.skip(skip);

        return newChunkedResponse(status, fin);
    }

    public static HttpResponse newChunkedResponse(@NonNull HttpStatus status, @NonNull InputStream responseStream) {
        return new HttpResponse(responseStream, TransferEncoding.CHUNKED, status);
    }

    /* ---------------- */
    /* Misc             */
    /* ---------------- */

    public static enum TransferEncoding {
        FIXED_LENGTH,
        CHUNKED;

    }

}
