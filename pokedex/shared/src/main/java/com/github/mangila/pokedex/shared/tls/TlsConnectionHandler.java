package com.github.mangila.pokedex.shared.tls;

import java.io.IOException;

public class TlsConnectionHandler {
    private TlsConnection connection;

    private TlsConnectionHandler(TlsConnection connection) {
        this.connection = connection;
    }

    public static TlsConnectionHandler create(String host, int port) {
        TlsConnection tlsConnection = TlsConnection.create(host, port);
        return new TlsConnectionHandler(tlsConnection);
    }

    public TlsConnectionHandler reconnectIfUnHealthy() {
        if (!connection.isConnected()) {
            reconnect();
            return this;
        }
        return this;
    }

    public void reconnect() {
        String host = connection.getHost();
        int port = connection.getPort();
        connection = TlsConnection.create(host, port);
        connection.connect();
    }

    public void writeAndFlush(byte[] bytes) throws IOException {
        connection.getOutputStream().write(bytes);
        connection.getOutputStream().flush();
    }

    public void disconnect() {
        connection.disconnect();
    }

    public TlsConnection getConnection() {
        return connection;
    }
}
