package com.github.mangila.pokedex.shared.https.client.json;

import com.github.mangila.pokedex.shared.json.JsonParser;
import com.github.mangila.pokedex.shared.tls.TlsConnectionPoolConfig;

public record JsonClientConfig(
        String host,
        JsonParser jsonParser,
        TlsConnectionPoolConfig poolConfig) {
}
