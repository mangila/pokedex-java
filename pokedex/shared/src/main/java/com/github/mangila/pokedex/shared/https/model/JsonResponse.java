package com.github.mangila.pokedex.shared.https.model;

import com.github.mangila.pokedex.shared.json.model.JsonTree;

import java.util.Objects;

public class JsonResponse {

    private final HttpStatus httpStatus;
    private final Headers headers;
    private final JsonTree body;

    private JsonResponse(Builder builder) {
        this.httpStatus = Objects.requireNonNull(builder.httpStatus, "httpStatus must not be null");
        this.headers = Objects.requireNonNull(builder.headers, "headers must not be null");
        this.body = Objects.requireNonNull(builder.body, "body must not be null");
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Headers getHeaders() {
        return headers;
    }

    public JsonTree getBody() {
        return body;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder toBuilder(JsonResponse other) {
        return new Builder()
                .httpStatus(other.getHttpStatus())
                .headers(other.getHeaders())
                .body(other.getBody());
    }

    public static class Builder {
        private HttpStatus httpStatus;
        private Headers headers;
        private JsonTree body;

        public Builder httpStatus(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder headers(Headers headers) {
            this.headers = headers;
            return this;
        }

        public Headers headers() {
            return headers;
        }

        public Builder body(JsonTree body) {
            this.body = body;
            return this;
        }

        public JsonResponse build() {
            return new JsonResponse(this);
        }
    }
}