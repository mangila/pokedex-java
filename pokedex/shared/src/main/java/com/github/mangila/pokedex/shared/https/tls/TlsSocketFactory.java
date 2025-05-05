package com.github.mangila.pokedex.shared.https.tls;

import com.github.mangila.pokedex.shared.https.config.SocketConfig;
import com.github.mangila.pokedex.shared.https.config.TlsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class TlsSocketFactory {

    private static final Logger log = LoggerFactory.getLogger(TlsSocketFactory.class);
    private static final SSLContext CONTEXT;

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
            CONTEXT = SSLContext.getInstance("TLS");
            // Default Java Keystore with some well-known certificates
            CONTEXT.init(null, null, SecureRandom.getInstanceStrong());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public static SSLSocket createClientSocket(
            SocketConfig socketConfig,
            TlsConfig tlsConfig
    ) {
        try {
            var socket = (SSLSocket) CONTEXT.getSocketFactory().createSocket();
            socket.setKeepAlive(socketConfig.keepAlive());
            socket.setSendBufferSize(socketConfig.sendBufferSize());
            socket.setReceiveBufferSize(socketConfig.receiveBufferSize());
            socket.setSoTimeout(socketConfig.soTimeoutMillis());
            socket.setSoLinger(socketConfig.soLinger(), socketConfig.soLingerTimeSeconds());
            socket.setTcpNoDelay(socketConfig.tcpNoDelay());
            SSLParameters params = socket.getSSLParameters();
            params.setEndpointIdentificationAlgorithm("HTTPS");
            params.setProtocols(tlsConfig.enabledProtocols());
            params.setApplicationProtocols(tlsConfig.applicationProtocols());
            socket.setSSLParameters(params);
            socket.addHandshakeCompletedListener(event -> {
                log.debug("Protocol version - {}", event.getSession().getProtocol());
                log.debug("Cipher Suite - {}", event.getCipherSuite());
                log.debug("Application Protocol - {}", event.getSocket().getApplicationProtocol());
            });
            socket.setUseClientMode(Boolean.TRUE);
            return socket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
