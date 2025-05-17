package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.queue.BoundedQueue;
import com.github.mangila.pokedex.shared.queue.BoundedQueueService;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class TlsConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(TlsConnectionPool.class);

    private final BoundedQueue queue;
    private final ScheduledExecutorService healthProbe;
    private final AtomicBoolean connected;
    private final TlsConnectionPoolConfig config;

    private int connectionCounter;

    public TlsConnectionPool(TlsConnectionPoolConfig config) {
        this.config = config;
        this.connected = new AtomicBoolean(Boolean.FALSE);
        this.queue = BoundedQueueService.getInstance().createNewBoundedQueue(
                config.poolConfig().poolName(),
                config.poolConfig().maxConnections());
        this.healthProbe = VirtualThreadConfig.newSingleThreadScheduledExecutor();
        this.connectionCounter = 0;
    }

    public void init() {
        log.debug("Initializing connections to the connection pool");
        var maxConnections = config.poolConfig().maxConnections();
        for (int i = 0; i < maxConnections; i++) {
            log.debug("Creating new connection - {} of {}", i + 1, maxConnections);
            try {
                addNewConnection();
            } catch (InterruptedException e) {
                log.error("ERR", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
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
        var poll = queue.poll(timeout);
        if (poll == null) {
            log.debug("No connection available");
            return Optional.empty();
        }
        var connection = poll.getDataAs(PooledTlsConnection.class)
                .reconnectIfUnHealthy();
        log.debug("Connection borrowed - {}", connection.id());
        return Optional.of(connection);
    }

    public PooledTlsConnection borrow() throws InterruptedException {
        ensureConnectionPoolIsInitialized();
        var connection = queue.take().getDataAs(PooledTlsConnection.class)
                .reconnectIfUnHealthy();
        log.debug("Connection borrowed - {}", connection.id());
        return connection;
    }

    public void returnConnection(PooledTlsConnection connection) throws IllegalStateException, InterruptedException {
        ensureConnectionPoolIsInitialized();
        log.debug("Returning connection to pool - {}", connection.id());
        queue.add(new QueueEntry(connection));
    }

    public boolean isConnected() {
        return connected.get();
    }

    public int poolSize() {
        return queue.available();
    }

    public void shutdownConnectionPool() {
        log.debug("Shutting down connection pool");
        healthProbe.shutdown();
        queue.forEach(queueEntry -> queueEntry
                .getDataAs(PooledTlsConnection.class)
                .disconnect());
        queue.clear();
        connected.set(Boolean.FALSE);
    }

    public void addNewConnection() throws InterruptedException {
        this.connectionCounter = connectionCounter + 1;
        var pooledTlsConnection = createNewConnection(connectionCounter);
        queue.add(new QueueEntry(pooledTlsConnection));
    }

    private PooledTlsConnection createNewConnection(int id) {
        try {
            var connection = new TlsConnection(config.host(), config.port());
            connection.connect();
            var pooledTlsConnection = connection.toPooledTlsConnection(id);
            log.debug("Created new connection {} - {}", connectionCounter, pooledTlsConnection.created());
            return pooledTlsConnection;
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
        log.info("Health probe started");
        if (queue.isEmpty()) {
            log.debug("Pool is empty, no connections to check");
            return;
        }
        queue.forEach(queueEntry -> {
            var connection = queueEntry.getDataAs(PooledTlsConnection.class);
            if (!connection.isConnected()) {
                log.debug("Connection {} is unhealthy, reconnecting", connection.id());
                connection.reconnect();
            }
        });
    }
}
