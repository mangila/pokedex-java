package com.github.mangila.pokedex.shared.pokeapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

public abstract class HttpsClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(HttpsClient.class);
    private static final int END_OF_STREAM = -1;
    private static final int DEFAULT_SEND_BUFFER_SIZE = 8192;
    private static final int DEFAULT_RECEIVE_BUFFER_SIZE = 1024 * 1024;
    private static final int DEFAULT_PORT = 443;
    private static final String[] DEFAULT_PROTOCOL = new String[]{"TLSv1.3"};

    private final SSLSocketFactory sslSocketFactory;
    private final PokeApiHost host;

    private SSLSocket socket;

    public final Function<GetRequest, Response> get = this::get;

    public HttpsClient(String host) {
        this.host = new PokeApiHost(host);
        this.sslSocketFactory = Tls.CONTEXT.getSocketFactory();
    }

    abstract Response get(GetRequest getRequest);

    /**
     * <summary>
     * Establishes a TLS/SSL connection with custom socket options for optimized performance and secure communication.
     * <p>
     * The following socket options are configured on the TCP socket:
     * <p>
     * - SO_SNDBUF (Send Buffer): Sets the socket's send buffer size to 8 KB, which is the amount of data the socket will buffer before sending.
     * <p>
     * - SO_RCVBUF (Receive Buffer): Configures the socket's receive buffer size to 1 MB to handle larger incoming data efficiently.
     * <p>
     * - SO_TIMEOUT: Sets the read timeout to 10 seconds, which prevents the socket from blocking indefinitely while waiting for a response.
     * <p>
     * - SO_LINGER: Enables linger behavior with a timeout of 100 ms, ensuring the socket closes gracefully even when there is still data being transmitted.
     * <p>
     * - TCP_NODELAY: Disables Nagle's algorithm (via TCP_NODELAY) to ensure small packets are sent immediately without delay, reducing latency.
     * <p>
     * - SO_KEEPALIVE: Enables keep-alive functionality, which ensures that the socket periodically checks if the connection is still active, useful for long-lived connections.
     * <p>
     * - Enabled Protocols: Specifies which SSL/TLS protocols (e.g., TLSv1.2, TLSv1.3) to use for securing the connection.
     * <p>
     * - Handshake Listener: Adds a listener that logs the cipher suite and Protocol used in the SSL handshake, allowing verification of encryption standards.
     * <p>
     * - setUseClientMode: Configures the socket to be in client mode (i.e., it will initiate a connection to a server). This is necessary for outgoing SSL/TLS connections.
     * <p>
     * This method connects the socket to the specified host and port, performs the SSL handshake, and establishes a secure connection.
     * </summary>
     */
    public void connect() {
        try {
            setSocket((SSLSocket) sslSocketFactory.createSocket());
            getSocket().setSendBufferSize(DEFAULT_SEND_BUFFER_SIZE);
            getSocket().setReceiveBufferSize(DEFAULT_RECEIVE_BUFFER_SIZE);
            getSocket().setSoTimeout((int) TimeUnit.SECONDS.toMillis(10));
            getSocket().setSoLinger(Boolean.TRUE, (int) TimeUnit.MILLISECONDS.toMillis(100));
            getSocket().setTcpNoDelay(Boolean.TRUE);
            getSocket().setKeepAlive(Boolean.TRUE);
            getSocket().setEnabledProtocols(DEFAULT_PROTOCOL);
            getSocket().addHandshakeCompletedListener(event -> {
                log.debug("Protocol version - {}", event.getSession().getProtocol());
                log.debug("Cipher Suite - {}", event.getCipherSuite());
            });
            getSocket().setUseClientMode(Boolean.TRUE);
            getSocket().connect(new InetSocketAddress(getHost(), DEFAULT_PORT));
            getSocket().startHandshake();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        if (Objects.nonNull(this.socket)) {
            try {
                this.socket.close();
            } catch (IOException e) {
                log.error("ERR", e);
            }
        }
    }

    String readGzipBody(Map<String, String> headers) throws IOException {
        var contentEncoding = headers.get("Content-Encoding");
        Objects.requireNonNull(contentEncoding, "Expected Content-Encoding header, but was null");
        if (!Objects.equals(contentEncoding, "gzip")) {
            throw new IOException("Expected gzip encoding, but got " + headers.get("Content-Encoding"));
        }
        var hasContentLength = headers.containsKey("Content-Length");
        if (!hasContentLength) {
            throw new IOException("Expected Content-Length header, but found none");
        }
        var gzip = new GZIPInputStream(getSocket().getInputStream());
        var contentLength = Integer.parseInt(headers.get("Content-Length"));
        var bodyBytes = new ByteArrayOutputStream(contentLength);
        var buffer = new byte[contentLength];
        int len;
        while ((len = gzip.read(buffer)) != END_OF_STREAM) {
            bodyBytes.write(buffer, 0, len);
        }
        String body = bodyBytes.toString(Charset.defaultCharset());
        log.debug("Body: {}", body);
        return body;
    }

    String readStatusCode() throws IOException {
        var statusCodeBuffer = new ByteArrayOutputStream();
        int previous = -1;
        int current = -1;
        while (true) {
            current = getSocket().getInputStream().read();
            if (current == END_OF_STREAM) {
                throw new IOException("Stream ended unexpectedly");
            }
            statusCodeBuffer.write(current);
            if (Utils.IsCrLf(previous, current) && statusCodeBuffer.size() >= 8) {
                var statusCode = statusCodeBuffer.toString(Charset.defaultCharset());
                log.debug("Status Code: {}", statusCode);
                return statusCode;
            }
            previous = current;
        }
    }

    Map<String, String> readHeaders() throws IOException {
        var map = new HashMap<String, String>();
        var headerBuffer = new ByteArrayOutputStream();
        int previous = -1;
        int current = -1;
        while (true) {
            current = getSocket().getInputStream().read();
            if (current == END_OF_STREAM) {
                throw new IOException("Stream ended unexpectedly");
            }
            headerBuffer.write(current);
            if (Utils.IsCrLf(previous, current)) {
                var header = headerBuffer.toString(Charset.defaultCharset());
                if (header.isBlank()) {
                    break;
                }
                var parts = header
                        .trim()
                        .split(": ");
                if (parts.length == 2) {
                    log.debug("Header: {} = {}", parts[0], parts[1]);
                    map.put(parts[0], parts[1]);
                }
                headerBuffer.reset();
            }
            previous = current;
        }
        return map;
    }

    public String getHost() {
        return host.host();
    }

    public SSLSocket getSocket() {
        return socket;
    }

    public void setSocket(SSLSocket socket) {
        this.socket = socket;
    }
}
