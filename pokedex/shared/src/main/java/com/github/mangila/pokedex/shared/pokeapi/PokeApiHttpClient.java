package com.github.mangila.pokedex.shared.pokeapi;

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

    private static final String HOST = "pokeapi.co";
    private static final int PORT = 443;
    private static final String[] PROTOCOL = new String[]{"TLSv1.2"};

    private static final SSLContext sslContext;
    private final SSLSocketFactory sslSocketFactory;
    private SSLSocket socket;

    static {
        /*
        *** ClientHello, [cipher suites and supported SSL/TLS versions]
        *** ServerHello, [cipher suite, session ID, server certificate]
        ...
        *** Certificate verification
        ...
        *** Finished handshake
         */
        System.setProperty("javax.net.debug", "ssl:handshake");
        try {
            sslContext = SSLContext.getInstance("TLS");
            // Default Java Keystore with some well-known certificates
            sslContext.init(null, null, SecureRandom.getInstanceStrong());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public PokeApiHttpClient() {
        this.sslSocketFactory = sslContext.getSocketFactory();
    }

    public void connect() {
        try {
            this.socket = (SSLSocket) sslSocketFactory.createSocket(HOST, PORT);
            socket.setUseClientMode(Boolean.TRUE);
            socket.setEnabledProtocols(PROTOCOL);
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
            var r = request.toRawHttpRequest(HOST);
            output.write(r.getBytes());
            output.flush();
            var statusCode = reader.readLine();
            System.out.println(statusCode);
        } catch (Exception e) {

        }
        return new byte[0];
    }

}
