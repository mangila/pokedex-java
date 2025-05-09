package com.github.mangila.pokedex.shared.https.model;

import java.util.List;
import java.util.stream.Collectors;

public record JsonRequest(
        String method,
        String path,
        List<Header> headers
) {

    public String toRequestLine(String version) {
        return String.format("GET %s %s", path, version.toUpperCase());
    }

    public String toHeaders() {
        return headers().stream()
                .map(Header::toHeaderLine)
                .collect(Collectors.joining("\n"));
    }

    public String toHttp(String host, String version) {
        return """
                %s
                Host: %s
                Connection: keep-alive
                Accept: application/json
                Accept-Encoding: gzip
                %s
                
                """.formatted(toRequestLine(version), host, toHeaders());
    }
}
