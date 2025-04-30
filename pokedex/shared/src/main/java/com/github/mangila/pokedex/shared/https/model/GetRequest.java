package com.github.mangila.pokedex.shared.https.model;

import java.util.Arrays;
import java.util.stream.Collectors;

public record GetRequest(
        String path,
        Header[] headers
) {

    public String toRequestLine(String version) {
        return String.format("GET %s %s", path, version.toUpperCase());
    }

    public String toHttp(String host, String version) {
        var headers = Arrays.stream(headers())
                .map(Header::toHeaderLine)
                .collect(Collectors.joining("\n"));
        return """
                %s
                Host: %s
                Connection: keep-alive
                Accept: application/json
                Accept-Encoding: gzip
                %s
                
                """.formatted(toRequestLine(version), host, headers);
    }
}
