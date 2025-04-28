package com.github.mangila.pokedex.shared.https;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PokeApiHttpClient {

    private static final SSLContext sslContext;
    private final SSLSocketFactory sslSocketFactory;
    private SSLSocket socket;

    static {
        System.setProperty("javax.net.debug", "ssl:handshake");
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, SecureRandom.getInstanceStrong());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public PokeApiHttpClient() {
        this.sslSocketFactory = sslContext.getSocketFactory();
    }

    public void connect(Request request) {
        try {
            this.socket = (SSLSocket) sslSocketFactory.createSocket(request.uri().getHost(), 443);
            socket.setUseClientMode(Boolean.TRUE);
            socket.setEnabledProtocols(new String[]{"TLSv1.2"});
            socket.startHandshake();
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

    public byte[] execute(Request request) {
        try {
            var input = socket.getInputStream();
            var output = socket.getOutputStream();
            var reader = new BufferedReader(new InputStreamReader(input));
            var r = request.toRawHttpRequest();
            output.write(r.getBytes());
            output.flush();
            var statusCode = reader.readLine();
            System.out.println(statusCode);

        } catch (Exception e) {

        }
        return new byte[0];
    }

}
