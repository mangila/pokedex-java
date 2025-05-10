package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TlsConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(TlsConnectionPool.class);

    private final BlockingQueue<PooledTlsConnection> pool;
    private final ScheduledExecutorService healthProbe;
    private final AtomicBoolean connected;
    private final TlsConnectionPoolConfig config;

    private int connectionCounter;

    public TlsConnectionPool(TlsConnectionPoolConfig config) {
        this.config = config;
        this.connected = new AtomicBoolean(Boolean.FALSE);
        this.pool = new ArrayBlockingQueue<>(config.maxConnections());
        this.healthProbe = VirtualThreadConfig.newSingleThreadScheduledExecutor();
        this.connectionCounter = 0;
    }

    public TlsConnectionPool(String host, int port) {
        this(new TlsConnectionPoolConfig(host, port, 5,
                new TlsConnectionPoolConfig.HealthCheckConfig(
                        0,
                        10,
                        TimeUnit.SECONDS
                )));
    }

    public void init() {
        log.debug("Initializing connections to the connection pool");
        var maxConnections = config.maxConnections();
        for (int i = 0; i < maxConnections; i++) {
            log.debug("Creating new connection - {} of {}", i + 1, maxConnections);
            addNewConnection();
        }
        connected.set(Boolean.TRUE);
        healthProbe.scheduleWithFixedDelay(this::healthProbe,
                config.healthCheckConfig().initialDelay(),
                config.healthCheckConfig().delay(),
                config.healthCheckConfig().timeUnit());
    }

    public Optional<PooledTlsConnection> borrow(Duration timeout) throws InterruptedException {
        ensureConnectionPoolIsInitialized();
        log.debug("Getting connection from pool");
        var connection = pool.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
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

    public PooledTlsConnection borrow() throws InterruptedException {
        ensureConnectionPoolIsInitialized();
        var connection = pool.take();
        if (!connection.isConnected()) {
            connection.reconnect();
        }
        log.debug("Connection borrowed - {}", connection.id());
        return connection;
    }

    public void returnConnection(PooledTlsConnection connection) throws IllegalStateException {
        ensureConnectionPoolIsInitialized();
        log.debug("Returning connection to pool - {}", connection.id());
        pool.add(connection);
    }

    public boolean isConnected() {
        return connected.get();
    }

    public int poolSize() {
        return pool.size();
    }

    public void shutdownConnectionPool() {
        log.debug("Shutting down connection pool");
        healthProbe.shutdown();
        pool.forEach(PooledTlsConnection::disconnect);
        pool.clear();
        connected.set(Boolean.FALSE);
    }

    public void addNewConnection() {
        this.connectionCounter = connectionCounter + 1;
        var connection = createNewConnection(connectionCounter);
        pool.add(connection);
        log.debug("Created new connection {} - {}", connectionCounter, connection.created());
    }

    private PooledTlsConnection createNewConnection(int id) {
        try {
            var connection = new TlsConnection(config.host(), config.port());
            connection.connect();
            return new PooledTlsConnection(id, connection, Instant.now());
        } catch (Exception e) {
            log.error("ERR", e);
            throw e;
        }
    }

    private void ensureConnectionPoolIsInitialized() {
        if (!isConnected()) {
            throw new IllegalStateException("Connection pool is not initialized");
        }
    }

    /**
     * Run a background virtual thread that checks health status and reconnects
     * if unhealthy
     */
    private void healthProbe() {
        if (pool.isEmpty()) {
            log.debug("Pool is empty, no connections to check");
            return;
        }
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
