package com.github.mangila.pokedex.shared.https.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract sealed class AbstractHeaders permits RequestHeaders, ResponseHeaders {

    private final Map<String, String> rawHeaders = new HashMap<>();
    private final List<Header> headers = new ArrayList<>();

    public void putRaw(String name, String value) {
        rawHeaders.put(name, value);
        headers.add(new Header(name, value));
    }

    public String getRaw(String name) {
        return rawHeaders.get(name);
    }

    boolean headerExists(String name) {
        return rawHeaders.containsKey(name);
    }

    public String toRawHeaders() {
        return headers.stream()
                .map(Header::toRawHeader)
                .collect(Collectors.joining("\n"));
    }
}
