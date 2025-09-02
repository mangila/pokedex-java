package com.github.mangila.pokedex.shared.https.tls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;

public class TlsClientSocketFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TlsClientSocketFactory.class);
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
        //   System.setProperty("javax.net.debug", "all");
        try {
            CONTEXT = SSLContext.getInstance("TLS");
            // Default Java Keystore with some well-known root CA certificates
            // If a certificate chains back to one of these root CAs, it’s trusted.
            CONTEXT.init(null, null, SecureRandom.getInstanceStrong());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public static SSLSocket create() {
        try {
            SSLSocket socket = (SSLSocket) CONTEXT.getSocketFactory().createSocket();
            setKeepAlive(socket);
            setSendBufferSize(socket);
            setReceiveBufferSize(socket);
            setSoTimeout(socket);
            setSoLinger(socket);
            setTcpNoDelay(socket);
            setSSLParameters(socket);
            socket.addHandshakeCompletedListener(handshakeCompletedEvent());
            socket.setUseClientMode(Boolean.TRUE);
            return socket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * sets the SO_KEEPALIVE flag
     * <p>
     * SO_KEEPALIVE basically tells the OS networking stack:
     * “Please monitor this TCP connection with keep-alive probes.”
     * </p>
     * <p>
     * OS will send keep-alive probes to the remote host to keep the connection alive.
     * If the remote host does not respond, the connection will be closed.
     * </p>
     * <p>
     * This is useful when using HTTP/1.1 connection pool and the HTTP header "Connection", "keep-alive".
     * </p>
     */
    private static void setKeepAlive(SSLSocket socket) {
        try {
            socket.setKeepAlive(Boolean.TRUE);
        } catch (SocketException e) {
            LOGGER.warn("Failed to set SO_KEEPALIVE on socket", e);
        }
    }

    /**
     * SO_SNDBUF: The size of the socket send buffer in bytes.
     *
     * @param socket
     */
    private static void setSendBufferSize(SSLSocket socket) {
        try {
            socket.setSendBufferSize(1024 * 8);
        } catch (SocketException e) {
            LOGGER.warn("Failed to set SO_SNDBUF on socket", e);
        }
    }

    /**
     * SO_RCVBUF: The size of the socket receive buffer in bytes.
     *
     * @param socket
     */
    private static void setReceiveBufferSize(SSLSocket socket) {
        try {
            socket.setReceiveBufferSize(1024 * 8);
        } catch (SocketException e) {
            LOGGER.warn("Failed to set SO_RCVBUF on socket", e);
        }
    }

    /**
     * SO_TIMEOUT: The timeout for waiting for data, in milliseconds.
     * <p>
     * When reading from socket, blocks until data is available, or the specified timeout is reached.
     * </p>
     *
     * @param socket
     */
    private static void setSoTimeout(SSLSocket socket) {
        try {
            Duration duration = Duration.ofSeconds(5);
            socket.setSoTimeout((int) duration.toMillis());
        } catch (SocketException e) {
            LOGGER.warn("Failed to set SO_TIMEOUT on socket", e);
        }
    }

    /**
     * SO_LINGER: The linger on a close option.
     * <p>
     * Try to send remaining data before closing the socket.
     * </p>
     *
     * @param socket
     */
    private static void setSoLinger(SSLSocket socket) {
        try {
            Duration duration = Duration.ofMillis(1000);
            int durationMillis = (int) duration.toMillis();
            socket.setSoLinger(true, durationMillis);
        } catch (SocketException e) {
            LOGGER.warn("Failed to set SO_LINGER on socket", e);
        }
    }

    /**
     * TCP_NODELAY: Disable the Nagle algorithm.
     * <p>
     * TCP_NODELAY basically tells the OS networking stack:
     * “Please don't wait for me to send the data, send it right away!”
     * </p>
     *
     * @param socket
     */
    private static void setTcpNoDelay(SSLSocket socket) {
        try {
            socket.setTcpNoDelay(true);
        } catch (SocketException e) {
            LOGGER.warn("Failed to set TCP_NODELAY on socket", e);
        }
    }

    /**
     * This configures the TLS/SSL parameters to be used for the connection.
     * <ul>
     *     <li>Enables endpoint identification. IMPORTANT!</li>
     *     <li>Negotiate only TLSv1.2 or TLSv.13</li>
     *     <li>Prefer http/1.1 as application protocol</li>
     * </ul>
     *
     * @param socket
     */
    private static void setSSLParameters(SSLSocket socket) {
        SSLParameters params = socket.getSSLParameters();
        params.setEndpointIdentificationAlgorithm("HTTPS");
        params.setProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
        params.setApplicationProtocols(new String[]{"http/1.1"});
        socket.setSSLParameters(params);
    }

    /**
     * Logs the handshake completed event.
     *
     * @return handshake completed listener
     */
    public static HandshakeCompletedListener handshakeCompletedEvent() {
        return event -> {
            LOGGER.debug("Protocol version - {}", event.getSession().getProtocol());
            LOGGER.debug("Cipher Suite - {}", event.getCipherSuite());
            LOGGER.debug("Application Protocol - {}", event.getSocket().getApplicationProtocol());
        };
    }
}
