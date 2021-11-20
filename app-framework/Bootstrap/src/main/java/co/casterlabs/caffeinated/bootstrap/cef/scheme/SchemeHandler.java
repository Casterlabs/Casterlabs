package co.casterlabs.caffeinated.bootstrap.cef.scheme;

import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.HttpRequest;
import co.casterlabs.caffeinated.bootstrap.cef.scheme.http.HttpResponse;
import lombok.NonNull;

public interface SchemeHandler {

    public @NonNull HttpResponse onRequest(@NonNull HttpRequest request);

}
