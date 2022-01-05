package co.casterlabs.caffeinated.bootstrap.webview.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefPostData;
import org.cef.network.CefPostDataElement;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import co.casterlabs.caffeinated.bootstrap.webview.scheme.SchemeHandler;
import co.casterlabs.caffeinated.bootstrap.webview.scheme.http.HttpMethod;
import co.casterlabs.caffeinated.bootstrap.webview.scheme.http.HttpRequest;
import co.casterlabs.caffeinated.bootstrap.webview.scheme.http.HttpResponse;
import co.casterlabs.caffeinated.bootstrap.webview.scheme.http.HttpResponse.TransferEncoding;
import co.casterlabs.rakurai.collections.HeaderMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class CefResponseResourceHandler implements CefResourceHandler {
    private final @NonNull SchemeHandler handler;

    private @Setter HttpResponse response;

    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) {
        HeaderMap requestHeaders = convertHeaders(request);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String uri = request.getURL();
//        String queryString = uri.split("\\?", 2)[1];

        // TODO parse queryString...
        // TODO request body(?)

        Vector<CefPostDataElement> elements = new Vector<CefPostDataElement>();
        CefPostData postData = request.getPostData();

        if (postData != null) {
            postData.getElements(elements);
        }

        HttpRequest httpRequest = new HttpRequest(
            requestHeaders,
            uri,
            null,
            null,
            null,
            method
//            elements
        );

        this.response = this.handler.onRequest(httpRequest);

        if (this.response == null) {
            callback.cancel();
            return false;
        } else {
            callback.Continue();
            return true;
        }
    }

    @Override
    public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUri) {
        response.setStatus(this.response.getStatus().getStatusCode());
//        response.setStatusText(this.response.getStatus().getDescription());

        if (this.response.getMode() == TransferEncoding.FIXED_LENGTH) {
            responseLength.set(this.response.getLength());
        }

        response.setHeaderMap(this.response.getAllHeaders());
        response.setMimeType(this.response.getAllHeaders().getOrDefault("content-type", "text/plain"));
    }

    @Override
    public boolean readResponse(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefCallback callback) {
        InputStream responseStream = this.response.getResponseStream();

        try {
            int read = responseStream.read(dataOut, 0, bytesToRead);

            if (read == -1) {
                return false; // No more data available.
            }

            bytesRead.set(read);

            callback.Continue();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            callback.cancel();
            return false;
        }
    }

    @Override
    public void cancel() {
        try {
            this.response.getResponseStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HeaderMap convertHeaders(CefRequest request) {
        Map<String, String> headers = new HashMap<>();

        request.getHeaderMap(headers);

        HeaderMap.Builder builder = new HeaderMap.Builder();

        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.put(header.getKey(), header.getValue());
        }

        return builder.build();
    }

}
