package co.casterlabs.caffeinated.bootstrap.cef.scheme.http;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.collections.HeaderMap;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class HttpRequest {
    private HeaderMap headers;
    private String uri;
    private Map<String, List<String>> allQueryParameters;
    private Map<String, String> queryParameters;
    private String queryString;
    private HttpMethod method;

    // Request headers
    public HeaderMap getHeaders() {
        return this.headers;
    }

    public final @Nullable String getHeader(@NonNull String header) {
        return this.getHeaders().getSingle(header);
    }

    // URI
    public String getUri() {
        return this.uri;
    }

    public Map<String, List<String>> getAllQueryParameters() {
        return this.allQueryParameters;
    }

    public Map<String, String> getQueryParameters() {
        return this.queryParameters;
    }

    public String getQueryString() {
        return this.queryString;
    }

    // Request body
    public final @Nullable String getBodyMimeType() {
        return this.getHeader("content-type");
    }

//    public abstract boolean hasBody();
//
//    public final @Nullable String getRequestBody() throws IOException {
//        if (this.hasBody()) {
//            return new String(this.getRequestBodyBytes(), StandardCharsets.UTF_8);
//        } else {
//            return null;
//        }
//    }
//
//    public final @NonNull JsonElement getRequestBodyJson(@Nullable Rson rson) throws IOException, JsonParseException {
//        if (this.hasBody()) {
//            if (rson == null) {
//                rson = Rson.DEFAULT;
//            }
//
//            if ("application/json".equals(this.getBodyMimeType())) {
//                String body = new String(this.getRequestBodyBytes(), StandardCharsets.UTF_8);
//
//                switch (body.charAt(0)) {
//                    case '{': {
//                        return rson.fromJson(body, JsonObject.class);
//                    }
//
//                    case '[': {
//                        return rson.fromJson(body, JsonArray.class);
//                    }
//
//                    default: {
//                        throw new JsonParseException("Request body must be either a JsonObject or JsonArray.");
//                    }
//                }
//            } else {
//                throw new JsonParseException("Request body must have a Content-Type of application/json.");
//            }
//        } else {
//            return null;
//        }
//    }
//
//    public abstract @Nullable byte[] getRequestBodyBytes() throws IOException;
//
//    public abstract Map<String, String> parseFormBody() throws IOException;

    // Misc
    public HttpMethod getMethod() {
        return this.method;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("HttpSession(");

        sb.append("\n    method=").append(this.getMethod());
        sb.append("\n    headers=").append(this.getHeaders());
        sb.append("\n    uri=").append(this.getUri()).append(this.getQueryString());

        sb.append("\n)");

        return sb.toString();
    }

}
