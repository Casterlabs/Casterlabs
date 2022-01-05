package co.casterlabs.caffeinated.bootstrap.webview.scheme;

import co.casterlabs.caffeinated.bootstrap.webview.scheme.http.HttpRequest;
import co.casterlabs.caffeinated.bootstrap.webview.scheme.http.HttpResponse;
import lombok.NonNull;

public interface SchemeHandler {

    public @NonNull HttpResponse onRequest(@NonNull HttpRequest request);

}
