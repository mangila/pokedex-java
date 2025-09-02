package com.github.mangila.pokedex.shared.https.http.json;

import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.https.tls.TlsConnectionPoolConfig;

public record JsonClientConfig(
        String host,
        JsonParser jsonParser,
        TlsConnectionPoolConfig poolConfig) {
}
