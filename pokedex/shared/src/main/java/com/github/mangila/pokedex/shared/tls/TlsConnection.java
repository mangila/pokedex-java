package com.github.mangila.pokedex.shared.tls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class TlsConnection {

    private static final Logger log = LoggerFactory.getLogger(TlsConnection.class);
    private final String host;
    private final int port;

    private SSLSocket socket;

    private TlsConnection(String host, int port) {
        this.host = host;
        this.port = port;
        this.socket = TlsClientSocketFactory.create();
    }

    public static TlsConnection create(String host, int port) {
        return new TlsConnection(host, port);
    }

    public boolean isConnected() {
        return socket.isConnected() &&
                !socket.isClosed() &&
                !socket.isInputShutdown() &&
                !socket.isOutputShutdown();
    }

    public void reconnect() {
        log.debug("Reconnecting to {}:{}", host, port);
        this.socket = TlsClientSocketFactory.create();
        connect();
    }

    public TlsConnection reconnectIfUnHealthy() {
        if (!isConnected()) {
            reconnect();
            return this;
        }
        return this;
    }

    public void connect() {
        log.debug("Connecting to {}:{}", host, port);
        try {
            this.socket.connect(new InetSocketAddress(host, port));
            this.socket.startHandshake();
        } catch (Exception e) {
            log.error("ERR", e);
            this.disconnect();
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            log.error("ERR", e);
        }
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }
}
