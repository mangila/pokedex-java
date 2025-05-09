package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TlsConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(TlsConnectionPool.class);
    private final ScheduledExecutorService healthProbe = VirtualThreadConfig.newSingleThreadScheduledExecutor();
    private final String host;
    private final int port;
    private final int maxConnections;
    private final BlockingQueue<PooledTlsConnection> pool;

    private final AtomicBoolean connected;

    public TlsConnectionPool(String host,
                             int port,
                             int maxConnections) {
        this.host = host;
        this.port = port;
        this.maxConnections = maxConnections;
        this.connected = new AtomicBoolean(Boolean.FALSE);
        this.pool = new ArrayBlockingQueue<>(maxConnections);
    }

    public TlsConnectionPool(String host, int port) {
        this(host, port, 5);
    }

    public Optional<PooledTlsConnection> borrow(Duration timeout) throws InterruptedException {
        log.debug("Getting connection from pool");
        var connection = pool.poll(timeout.toSecondsPart(), TimeUnit.SECONDS);
        if (connection == null) {
            log.debug("No connection available");
            return Optional.empty();
        }
        if (!connection.isConnected()) {
            connection.reconnect();
        }
        log.debug("Connection borrowed - {}", connection.id());
        return Optional.of(connection);
    }

    public void put(PooledTlsConnection connection) {
        log.debug("Returning connection to pool - {}", connection.id());
        if (connection.isConnected()) {
            pool.add(connection);
        } else {
            connection.disconnect();
            connection.reconnect();
            pool.add(connection);
        }
    }

    public TlsConnectionPool connect() throws InterruptedException {
        log.debug("Connecting to {}:{}", host, port);
        for (int i = 0; i < maxConnections; i++) {
            log.debug("Creating new connection - {} of {}", i + 1, maxConnections);
            pool.put(Objects.requireNonNull(createNewConnection(i + 1)));
        }
        connected.set(Boolean.TRUE);
        healthProbe.scheduleWithFixedDelay(this::healthProbe, 0, 10, TimeUnit.SECONDS);
        return this;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public int poolSize() {
        return pool.size();
    }

    public void shutdown() {
        log.debug("Shutting down connection pool");
        healthProbe.shutdown();
        pool.forEach(PooledTlsConnection::disconnect);
        connected.set(Boolean.FALSE);
    }

    public record PooledTlsConnection(TlsConnection connection,
                                      int id,
                                      Instant created) {
        public PooledTlsConnection {
            Objects.requireNonNull(connection);
            Objects.requireNonNull(created);
        }

        public void disconnect() {
            log.debug("Disconnecting connection {}", id);
            connection.disconnect();
        }

        public boolean isConnected() {
            return connection.isConnected();
        }

        public String getHttpVersion() {
            return connection.getHttpVersion();
        }

        public Instant getCreated() {
            return created;
        }

        public void reconnect() {
            log.debug("Reconnecting connection {}", id);
            connection.reconnect();
        }
    }

    private PooledTlsConnection createNewConnection(int id) {
        try {
            var connection = new TlsConnection(host, port);
            connection.connect();
            var pooledConnection = new PooledTlsConnection(connection, id, Instant.now());
            log.debug("Created new connection {} - {}", id, pooledConnection.getCreated());
            return pooledConnection;
        } catch (Exception e) {
            log.error("ERR", e);
        }
        return null;
    }

    /**
     * Run a background virtual thread that checks health status and reconnects
     * if unhealthy
     */
    private void healthProbe() {
        for (var connection : pool) {
            if (!connection.isConnected()) {
                log.debug("Connection {} is not connected, trying to reconnect", connection.id);
                connection.reconnect();
            } else {
                log.debug("Connection {} is connected", connection.id);
            }
        }
    }

}
