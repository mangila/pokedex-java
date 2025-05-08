package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.https.config.ConfigLoader;
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
    private final SSLSocket socket;

    public TlsConnection(String host, int port) {
        this.host = host;
        this.port = port;
        this.socket = TlsClientSocketFactory.create(
                ConfigLoader.socketConfig(), ConfigLoader.tlsConfig()
        );
    }

    public boolean isConnected() {
        return socket.isConnected() &&
                !socket.isClosed() &&
                !socket.isInputShutdown() &&
                !socket.isOutputShutdown();
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

    public String getHttpVersion() {
        return socket.getApplicationProtocol();
    }
}
