package com.github.mangila.pokedex.shared.pokeapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

class HttpsClient {

    private static final Logger log = LoggerFactory.getLogger(HttpsClient.class);
    private static final int END_OF_STREAM = -1;
    private static final int DEFAULT_SEND_BUFFER_SIZE = 8192;
    private static final int DEFAULT_RECEIVE_BUFFER_SIZE = 1024 * 1024;
    private static final int DEFAULT_PORT = 443;
    private static final String[] DEFAULT_PROTOCOL = new String[]{"TLSv1.3"};
    private static final String DEFAULT_VERSION = "http/1.1";
    private static final ResponseTtlCache TTL_CACHE = new ResponseTtlCache();

    private final String host;
    private final SSLSocketFactory sslSocketFactory;

    private SSLSocket socket;
    private OutputStream socketWrite;
    private InputStream socketRead;

    public HttpsClient(String host) {
        this.host = host;
        this.sslSocketFactory = Tls.CONTEXT.getSocketFactory();
    }

    Response get(GetRequest getRequest) {
        try {
            var request = getRequest.toHttp(this.host, this.socket.getApplicationProtocol());
            this.socketWrite.write(request.getBytes());
            this.socketWrite.flush();
            var statusLine = readStatusLine();
            var headers = readHeaders();
            var body = readGzipBody(headers);
            return new Response(statusLine, headers, body);
        } catch (Exception e) {
            log.error("ERR", e);
            disconnect();
        }
        return new Response("", Map.of(), "");
    }

    /**
     * Establishes a TLS/SSL connection with custom socket options for optimized performance and secure communication.
     * <p>
     * The following socket options are configured on the TCP socket:
     * <ul>
     *   <li><b>SO_SNDBUF (Send Buffer):</b> Sets the socket's send buffer size to 8 KB, which is the amount of data the socket will buffer before sending.</li>
     *   <li><b>SO_RCVBUF (Receive Buffer):</b> Configures the socket's receive buffer size to 1 MB to handle larger incoming data efficiently.</li>
     *   <li><b>SO_TIMEOUT:</b> Sets the read timeout to 10 seconds, which prevents the socket from blocking indefinitely while waiting for a response.</li>
     *   <li><b>SO_LINGER:</b> Enables linger behavior with a timeout of 1 second, ensuring the socket closes gracefully even when there is still data being transmitted.</li>
     *   <li><b>TCP_NODELAY:</b> Disables Nagle's algorithm (via TCP_NODELAY) to ensure small packets are sent immediately without delay, reducing latency.</li>
     *   <li><b>SO_KEEPALIVE:</b> Enables keep-alive functionality, which ensures that the socket periodically checks if the connection is still active, useful for long-lived connections.</li>
     * </ul>
     *
     * <p>
     * SSL/TLS-specific configurations include:
     * <ul>
     *   <li><b>Enabled Protocols:</b> Specifies which SSL/TLS protocols (e.g., TLSv1.2, TLSv1.3) to use for securing the connection.</li>
     *   <li><b>Endpoint Identification:</b> Enables hostname verification using HTTPS algorithm, which validates the server’s certificate against the target hostname.</li>
     *   <li><b>ALPN (Application-Layer Protocol Negotiation):</b> Configures the list of supported application protocols (e.g., HTTP/2, HTTP/1.1) to be negotiated during the TLS handshake.
     *       The chosen protocol can be retrieved after handshake using {@code SSLSocket.getApplicationProtocol()}.</li>
     *   <li><b>Handshake Listener:</b> Adds a listener that logs the cipher suite, negotiated protocol, and selected application-level protocol after the SSL handshake, allowing verification of encryption and protocol negotiation details.</li>
     *   <li><b>Client Mode:</b> Configures the socket to be in client mode (i.e., it will initiate a connection to a server). This is necessary for outgoing SSL/TLS connections.</li>
     * </ul>
     *
     * <p>
     * This method:
     * <ul>
     *   <li>Creates and configures an SSL socket with the specified options.</li>
     *   <li>Performs the SSL handshake and establishes a secure connection with the specified hostName and port.</li>
     *   <li>Verifies the identity of the remote hostName using HTTPS hostname verification.</li>
     *   <li>Negotiates an application-level protocol via ALPN, if supported.</li>
     *   <li>Logs the negotiated cipher suite, SSL/TLS version, and application protocol after the handshake.</li>
     * </ul>
     *
     * <p><b>Note:</b> This implementation assumes Java 9+ where ALPN support is available in the standard library via {@code SSLSocket} and {@code SSLParameters}.
     *
     * @throws RuntimeException If an I/O error occurs during the connection or handshake process.
     */
    public void connect() {
        try {
            log.debug("Connecting to {}:{}", this.host, DEFAULT_PORT);
            this.socket = (SSLSocket) sslSocketFactory.createSocket();
            this.socket.setSendBufferSize(DEFAULT_SEND_BUFFER_SIZE);
            this.socket.setReceiveBufferSize(DEFAULT_RECEIVE_BUFFER_SIZE);
            this.socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(10));
            this.socket.setSoLinger(Boolean.TRUE, (int) TimeUnit.SECONDS.toSeconds(1));
            this.socket.setTcpNoDelay(Boolean.TRUE);
            this.socket.setKeepAlive(Boolean.TRUE);
            this.socket.setEnabledProtocols(DEFAULT_PROTOCOL);
            SSLParameters params = socket.getSSLParameters();
            params.setEndpointIdentificationAlgorithm("HTTPS");
            params.setApplicationProtocols(new String[]{DEFAULT_VERSION});
            socket.setSSLParameters(params);
            this.socket.addHandshakeCompletedListener(event -> {
                log.debug("Protocol version - {}", event.getSession().getProtocol());
                log.debug("Cipher Suite - {}", event.getCipherSuite());
                log.debug("Application Protocol - {}", event.getSocket().getApplicationProtocol());
            });
            this.socket.setUseClientMode(Boolean.TRUE);
            this.socket.connect(new InetSocketAddress(this.host, DEFAULT_PORT));
            this.socket.startHandshake();
            this.socketRead = this.socket.getInputStream();
            this.socketWrite = this.socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        if (Objects.nonNull(this.socket) && !this.socket.isClosed()) {
            try {
                this.socket.close();
            } catch (IOException e) {
                log.error("ERR", e);
            }
        }
    }

    private String readGzipBody(Map<String, String> headers) throws IOException {
        var contentEncoding = headers.get("Content-Encoding");
        Objects.requireNonNull(contentEncoding, "Expected Content-Encoding header, but was null");
        if (!Objects.equals(contentEncoding, "gzip")) {
            throw new IOException("Expected gzip encoding, but got " + headers.get("Content-Encoding"));
        }
        var hasContentLength = headers.containsKey("Content-Length");
        if (!hasContentLength) {
            throw new IOException("Expected Content-Length header, but found none");
        }
        var contentLength = Integer.parseInt(headers.get("Content-Length"));
        var gzip = new GZIPInputStream(this.socketRead, contentLength);
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

    private String readStatusLine() throws IOException {
        var statusCodeBuffer = new ByteArrayOutputStream();
        int previous = -1;
        int current = -1;
        while (true) {
            current = this.socketRead.read();
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

    private Map<String, String> readHeaders() throws IOException {
        var map = new HashMap<String, String>();
        var headerBuffer = new ByteArrayOutputStream();
        int previous = -1;
        int current = -1;
        while (true) {
            current = this.socketRead.read();
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
}
