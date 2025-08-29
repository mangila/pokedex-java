package com.github.mangila.pokedex.shared.tls;

import java.io.IOException;

public class TlsConnectionHandler {
    private TlsConnection tlsConnection;

    private TlsConnectionHandler(TlsConnection tlsConnection) {
        this.tlsConnection = tlsConnection;
    }

    public static TlsConnectionHandler create(String host, int port) {
        TlsConnection tlsConnection = TlsConnection.create(host, port);
        return new TlsConnectionHandler(tlsConnection);
    }

    public TlsConnectionHandler reconnectIfUnHealthy() {
        if (!connected()) {
            reconnect();
            return this;
        }
        return this;
    }

    public void reconnect() {
        String host = tlsConnection.getHost();
        int port = tlsConnection.getPort();
        tlsConnection = TlsConnection.create(host, port);
        tlsConnection.connect();
    }

    public void writeAndFlush(byte[] bytes) throws IOException {
        tlsConnection.getOutputStream().write(bytes);
        tlsConnection.getOutputStream().flush();
    }

    public void disconnect() {
        tlsConnection.disconnect();
    }

    public TlsConnection getTlsConnection() {
        return tlsConnection;
    }

    public boolean connected() {
        return tlsConnection.connected();
    }
}
