package com.github.mangila.pokedex.shared.https.model;

import java.util.HashMap;
import java.util.Map;

public class Headers {

    private final Map<String, String> headers = new HashMap<>();

    public void put(String name, String value) {
        headers.put(name, value);
    }

    public String get(String name) {
        return headers.get(name);
    }

    public boolean headerExists(String name) {
        return headers.containsKey(name);
    }

    public boolean isGzip() {
        return headerExists("Content-Encoding") && headers.get("Content-Encoding").contains("gzip");
    }

    public boolean isJson() {
        return headerExists("Content-Type") && headers.get("Content-Type").contains("application/json");
    }

    public boolean isChunked() {
        return headerExists("Transfer-Encoding") && headers.get("Transfer-Encoding").contains("chunked");
    }

}
