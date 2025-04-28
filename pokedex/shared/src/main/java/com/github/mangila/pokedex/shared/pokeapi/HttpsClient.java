package com.github.mangila.pokedex.shared.pokeapi;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

public abstract class HttpsClient {

    public static final String[] DEFAULT_PROTOCOL = new String[]{"TLSv1.2"};
    private final SSLSocketFactory sslSocketFactory;

    private final String host;
    private SSLSocket socket;

    public HttpsClient(String host) {
        this.host = host;
        this.sslSocketFactory = Ssl.CONTEXT.getSocketFactory();
    }

    public String getHost() {
        return host;
    }

    public SSLSocket createSocket() throws IOException {
        return (SSLSocket) sslSocketFactory.createSocket(host, 443);
    }

    public SSLSocket getSocket() {
        return socket;
    }

    public void setSocket(SSLSocket socket) {
        this.socket = socket;
    }

    public void connect() {
        try {
            setSocket(createSocket());
            getSocket().setUseClientMode(Boolean.TRUE);
            getSocket().setEnabledProtocols(DEFAULT_PROTOCOL);
            getSocket().startHandshake();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public abstract Response execute(Request request);
}
