package co.casterlabs.caffeinated.webview.scheme;

import co.casterlabs.caffeinated.webview.scheme.http.HttpRequest;
import co.casterlabs.caffeinated.webview.scheme.http.HttpResponse;
import lombok.NonNull;

public interface SchemeHandler {

    public @NonNull HttpResponse onRequest(@NonNull HttpRequest request);

}
