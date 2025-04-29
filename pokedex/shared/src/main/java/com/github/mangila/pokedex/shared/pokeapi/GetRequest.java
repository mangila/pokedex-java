package com.github.mangila.pokedex.shared.pokeapi;

import java.util.Arrays;
import java.util.stream.Collectors;

public record GetRequest(
        String path,
        Header[] headers
) {

    public String toRequestLine() {
        return String.format("GET %s HTTP/1.1", path);
    }

    public String toHttp(String host) {
        var headers = Arrays.stream(headers())
                .map(Header::toHeaderLine)
                .collect(Collectors.joining("\n"));
        return """
                %s
                Host: %s
                Connection: keep-alive
                %s
                
                """.formatted(toRequestLine(), host, headers);
    }
}
