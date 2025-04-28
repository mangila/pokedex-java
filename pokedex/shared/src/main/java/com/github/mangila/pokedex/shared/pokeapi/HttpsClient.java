package com.github.mangila.pokedex.shared.pokeapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public abstract class HttpsClient {

    private static final Logger log = LoggerFactory.getLogger(HttpsClient.class);
    private static final String[] DEFAULT_PROTOCOL = new String[]{"TLSv1.2"};

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

    public SSLSocket getSocket() {
        return socket;
    }

    public void setSocket(SSLSocket socket) {
        this.socket = socket;
    }

    /**
     * Establishes an SSL connection with custom socket options for optimized performance and secure communication.
     * <p>
     * The following socket options are configured:
     * <p>
     * - **SO_REUSEADDR**: Allows the socket to bind to an address that is already in use (useful for quickly restarting a server).
     * <p>
     * - **SO_SNDBUF (Send Buffer)**: Sets the socket's **send buffer size** to 8 KB, which is the amount of data the socket will buffer before sending.
     * <p>
     * - **SO_RCVBUF (Receive Buffer)**: Configures the socket's **receive buffer size** to 1 MB to handle larger incoming data efficiently.
     * <p>
     * - **SO_TIMEOUT**: Sets the **read timeout** to 10 seconds, which prevents the socket from blocking indefinitely while waiting for a response.
     * <p>
     * - **SO_LINGER**: Enables **linger** behavior with a timeout of 10 seconds, ensuring the socket closes gracefully even when there is still data being transmitted.
     * <p>
     * - **TCP_NODELAY**: Disables **Nagle's algorithm** (via **TCP_NODELAY**) to ensure small packets are sent immediately without delay, reducing latency.
     * <p>
     * - **SO_KEEPALIVE**: Enables **keep-alive** functionality, which ensures that the socket periodically checks if the connection is still active, useful for long-lived connections.
     * <p>
     * - **Enabled Protocols**: Specifies which **SSL/TLS protocols** (e.g., TLSv1.2, TLSv1.3) to use for securing the connection.
     * <p>
     * - **Handshake Listener**: Adds a listener that logs the **cipher suite** used in the SSL handshake, allowing verification of encryption standards.
     * <p>
     * This method connects the socket to the specified host and port, performs the SSL handshake, and establishes a secure connection.
     */
    public void connect() {
        try {
            setSocket((SSLSocket) sslSocketFactory.createSocket());
            getSocket().setUseClientMode(Boolean.TRUE);
            getSocket().setSendBufferSize(8192);
            getSocket().setReceiveBufferSize(1024 * 1024);
            getSocket().setSoTimeout((int) TimeUnit.SECONDS.toMillis(10));
            getSocket().setSoLinger(Boolean.TRUE, (int) TimeUnit.SECONDS.toSeconds(10));
            getSocket().setTcpNoDelay(Boolean.TRUE);
            getSocket().setKeepAlive(Boolean.TRUE);
            getSocket().setEnabledProtocols(DEFAULT_PROTOCOL);
            getSocket().addHandshakeCompletedListener(event -> {
                log.debug("Cipher Suite - {}", event.getCipherSuite());
            });
            getSocket().connect(new InetSocketAddress(getHost(), 443));
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
