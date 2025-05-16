package com.github.mangila.pokedex.shared.https.model;

import java.util.List;
import java.util.stream.Collectors;

public record JsonRequest(
        String method,
        String path,
        List<RequestHeader> requestHeaders
) {

    public String toRequestLine(String version) {
        return String.format("%S %s %s", method, path, version.toUpperCase());
    }

    public String toHeaders() {
        return requestHeaders().stream()
                .map(RequestHeader::toHeaderLine)
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
