package com.github.mangila.pokedex.shared.config;

public record SocketConfig(
        boolean keepAlive,
        int sendBufferSize,
        int receiveBufferSize,
        int soTimeoutMillis,
        boolean soLinger,
        int soLingerTimeSeconds,
        boolean tcpNoDelay) {
}
