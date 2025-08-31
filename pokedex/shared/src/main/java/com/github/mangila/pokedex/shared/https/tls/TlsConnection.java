package com.github.mangila.pokedex.shared.https.tls;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record TlsConnection(String host, int port, SSLSocket socket) {

    public static TlsConnection create(String host, int port) {
        SSLSocket socket = TlsClientSocketFactory.create();
        return new TlsConnection(host, port, socket);
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }
}
