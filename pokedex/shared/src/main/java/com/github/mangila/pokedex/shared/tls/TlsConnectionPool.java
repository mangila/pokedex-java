package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TlsConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(TlsConnectionPool.class);

    private final String host;
    private final int port;
    private final int maxConnections;
    private final AtomicBoolean connected;
    private final BlockingQueue<PooledTlsConnection> pool;
    private final ScheduledExecutorService healthProbe;

    public TlsConnectionPool(TlsConnectionPoolConfig config) {
        this.host = config.host();
        this.port = config.port();
        this.maxConnections = config.maxConnections();
        this.connected = new AtomicBoolean(Boolean.FALSE);
        this.pool = new ArrayBlockingQueue<>(maxConnections);
        this.healthProbe = VirtualThreadConfig.newSingleThreadScheduledExecutor();
    }

    public TlsConnectionPool(String host, int port) {
        this(new TlsConnectionPoolConfig(host, port, 5));
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

    public void giveBack(PooledTlsConnection connection) {
        log.debug("Returning connection to pool - {}", connection.id());
        if (connection.isConnected()) {
            pool.add(connection);
        } else {
            connection.disconnect();
            connection.reconnect();
            pool.add(connection);
        }
    }

    public TlsConnectionPool connect() {
        log.debug("Connecting to {}:{}", host, port);
        for (int i = 0; i < maxConnections; i++) {
            log.debug("Creating new connection - {} of {}", i + 1, maxConnections);
            addNewConnection();
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

    public void addNewConnection() {
        var random = ThreadLocalRandom.current().nextInt(0, 100);
        pool.add(Objects.requireNonNull(createNewConnection(random)));
    }

    private PooledTlsConnection createNewConnection(int id) {
        try {
            var connection = new TlsConnection(host, port);
            connection.connect();
            var pooledConnection = new PooledTlsConnection(id, connection, Instant.now());
            log.debug("Created new connection {} - {}", id, pooledConnection.created());
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
                log.debug("Connection {} is not connected, trying to reconnect", connection.id());
                connection.reconnect();
            } else {
                log.debug("Connection {} is connected", connection.id());
            }
        }
    }

}
