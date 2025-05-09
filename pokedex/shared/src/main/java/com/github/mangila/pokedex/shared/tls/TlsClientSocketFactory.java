package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.config.SocketConfig;
import com.github.mangila.pokedex.shared.config.TlsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;

public class TlsClientSocketFactory {

    private static final Logger log = LoggerFactory.getLogger(TlsClientSocketFactory.class);
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
      //  System.setProperty("javax.net.debug", "ssl:handshake");
        try {
            CONTEXT = SSLContext.getInstance("TLS");
            // Default Java Keystore with some well-known certificates
            CONTEXT.init(null, null, SecureRandom.getInstanceStrong());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public static SSLSocket create() {
        return create(
                new SocketConfig(
                        new SocketConfig.KeepAlive(Boolean.TRUE),
                        new SocketConfig.BufferSize(1024 * 8, 1024 * 8),
                        new SocketConfig.SoTimeout(Duration.ofSeconds(5)),
                        new SocketConfig.SoLinger(Boolean.TRUE, 1),
                        new SocketConfig.TcpNoDelay(Boolean.TRUE)
                ),
                new TlsConfig(new String[]{"TLSv1.3"}, new String[]{"http/1.1"})
        );
    }

    public static SSLSocket create(
            SocketConfig socketConfig,
            TlsConfig tlsConfig
    ) {
        try {
            var socket = (SSLSocket) CONTEXT.getSocketFactory().createSocket();
            socket.setKeepAlive(socketConfig.keepAlive().active());
            socket.setSendBufferSize(socketConfig.bufferSize().send());
            socket.setReceiveBufferSize(socketConfig.bufferSize().receive());
            socket.setSoTimeout(socketConfig.soTimeout().duration().toMillisPart());
            socket.setSoLinger(socketConfig.soLinger().active(), socketConfig.soLinger().seconds());
            socket.setTcpNoDelay(socketConfig.tcpNoDelay().active());
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
