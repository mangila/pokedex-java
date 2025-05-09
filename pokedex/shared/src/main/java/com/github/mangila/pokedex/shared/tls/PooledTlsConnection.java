package com.github.mangila.pokedex.shared.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Objects;

public record PooledTlsConnection(int id, TlsConnection connection, Instant created) {

    public PooledTlsConnection {
        Objects.requireNonNull(connection);
        Objects.requireNonNull(created);
    }

    public InputStream getInputStream() throws IOException {
        return connection.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return connection.getOutputStream();
    }

    public void disconnect() {
        connection.disconnect();
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public void reconnect() {
        connection.reconnect();
    }
}
