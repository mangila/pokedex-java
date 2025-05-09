package com.github.mangila.pokedex.shared.tls.config;

public record TlsConnectionPoolConfig(String host, int port, int maxConnections) {
}
