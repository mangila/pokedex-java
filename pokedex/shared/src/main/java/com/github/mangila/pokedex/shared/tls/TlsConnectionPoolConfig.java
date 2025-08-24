package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.util.Ensure;

public record TlsConnectionPoolConfig(String host,
                                      int port,
                                      int maxConnections) {
    public TlsConnectionPoolConfig {
        Ensure.notNull(host, "host property must not be null");
        Ensure.min(1, port);
        Ensure.min(1, maxConnections);
    }
}
