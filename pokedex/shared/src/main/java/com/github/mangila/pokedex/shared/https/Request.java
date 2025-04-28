package com.github.mangila.pokedex.shared.https;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

public record Request(
        URI uri,
        String method,
        Header[] headers
) {

    public String toRequestLine() {
        return method + " " + uri.getPath() + " HTTP/1.1";
    }

    public String toHostHeader() {
        return "Host: " + uri.getHost();
    }

    public String toRawHttpRequest() {
        return toRequestLine() + "\n" +
                toHostHeader() + "\n" +
                "Connection: keep-alive\n" +
                Arrays.stream(headers)
                        .map(Header::toHeaderLine)
                        .collect(Collectors.joining("\n")) +
                "\r\n\r\n";
    }
}
