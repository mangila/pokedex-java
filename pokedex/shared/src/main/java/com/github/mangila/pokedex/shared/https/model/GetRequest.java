package com.github.mangila.pokedex.shared.https.model;

public record GetRequest(
        String path,
        RequestHeaders requestHeaders) {

    public static GetRequest from(String path) {
        return new GetRequest(path, RequestHeaders.DEFAULT);
    }

    public String toRawHeaders() {
        return requestHeaders.toRawHeaders();
    }

    private String toRequestLine() {
        return String.format("GET %s HTTP/1.1", path);
    }

    public String toRawHttp(String host) {
        return """
                %s
                Host: %s
                %s
                
                """.formatted(toRequestLine(), host, toRawHeaders());
    }
}
