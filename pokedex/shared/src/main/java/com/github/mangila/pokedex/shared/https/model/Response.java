package com.github.mangila.pokedex.shared.https.model;

import java.util.Map;

public record Response(String statusLine,
                       Map<String, String> headers,
                       String body) {
}
