package co.casterlabs.caffeinated.cef.scheme;

import co.casterlabs.caffeinated.cef.scheme.http.HttpRequest;
import co.casterlabs.caffeinated.cef.scheme.http.HttpResponse;
import lombok.NonNull;

public interface SchemeHandler {

    public @NonNull HttpResponse onRequest(@NonNull HttpRequest request);

}
