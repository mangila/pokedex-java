package com.github.mangila.pokedex.shared.https.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an abstract base class to manage HTTP headers.
 * This class provides common functionality for handling raw HTTP headers
 * and their corresponding parsed representations as {@code Header} objects.
 */
public abstract sealed class AbstractHeaders permits RequestHeaders, ResponseHeaders {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHeaders.class);
    private final Map<String, String> rawHeaders = new HashMap<>();
    private final List<Header> headers = new ArrayList<>();

    public void putRaw(String key, String value) {
        LOGGER.debug("Header: {} - {}", key, value);
        rawHeaders.put(key, value);
        headers.add(new Header(key, value));
    }

    public String getRaw(String key) {
        return rawHeaders.get(key);
    }

    boolean headerExists(String key) {
        return rawHeaders.containsKey(key);
    }

    public String toRawHeaders() {
        return headers.stream()
                .map(Header::toRawHeader)
                .collect(Collectors.joining("\n"));
    }
}
