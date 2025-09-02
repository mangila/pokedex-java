package com.github.mangila.pokedex.shared.https.tls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class TlsConnectionHandle {
    private static final Logger LOGGER = LoggerFactory.getLogger(TlsConnectionHandle.class);
    private TlsConnection tlsConnection;

    private TlsConnectionHandle(TlsConnection tlsConnection) {
        this.tlsConnection = tlsConnection;
    }

    public static TlsConnectionHandle create(String host, int port) {
        TlsConnection tlsConnection = TlsConnection.create(host, port);
        return new TlsConnectionHandle(tlsConnection);
    }

    public boolean isConnected() {
        SSLSocket socket = tlsConnection.socket();
        return socket.isConnected() &&
               !socket.isClosed() &&
               !socket.isInputShutdown() &&
               !socket.isOutputShutdown();
    }

    public TlsConnectionHandle reconnectIfUnHealthy() {
        if (!isConnected()) {
            reconnect();
            return this;
        }
        return this;
    }

    public void reconnect() {
        String host = tlsConnection.host();
        int port = tlsConnection.port();
        TlsConnection oldConnection = tlsConnection;
        try {
            TlsConnection newConnection = TlsConnection.create(host, port);
            SSLSocket socket = newConnection.socket();
            socket.connect(new InetSocketAddress(host, port));
            socket.startHandshake();
            tlsConnection = newConnection;
            try {
                oldConnection.socket().close();
            } catch (IOException e) {
                LOGGER.warn("Failed to close old socket during reconnect", e);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to reconnect socket {} {}", host, port, e);
            throw new RuntimeException("Reconnection failed", e);
        }
    }

    public void writeAndFlush(byte[] bytes) throws IOException {
        outputStream().write(bytes);
        outputStream().flush();
    }

    public void disconnect() {
        try {
            tlsConnection.socket().close();
        } catch (IOException e) {
            LOGGER.warn("Failed to close socket during disconnect", e);
        }
    }

    public InputStream inputStream() throws IOException {
        return tlsConnection.getInputStream();
    }

    public OutputStream outputStream() throws IOException {
        return tlsConnection.getOutputStream();
    }
}
