package com.github.mangila.pokedex.shared.tls.pool;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.tls.TlsConnection;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TlsConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(TlsConnectionPool.class);
    private static final Duration TIMEOUT = Duration.ofMinutes(1);

    private final TlsConnectionPoolConfig config;
    private final int maxConnections;
    private final BlockingQueue<TlsConnection> queue;
    private final HealthProbe healthProbe;
    private final AtomicBoolean initialized;

    public TlsConnectionPool(TlsConnectionPoolConfig config) {
        this.config = config;
        this.maxConnections = config.poolConfig().maxConnections();
        this.queue = new LinkedBlockingQueue<>(maxConnections);
        this.healthProbe = new HealthProbe(this.queue);
        this.initialized = new AtomicBoolean(Boolean.FALSE);
    }

    public void init() {
        initialized.set(Boolean.TRUE);
        for (int i = 0; i < maxConnections; i++) {
            log.debug("Creating new connection - {} of {}", i + 1, maxConnections);
            var connection = createNewConnection();
            offer(connection);
        }
        VirtualThreadConfig.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(healthProbe, 1, 30, TimeUnit.SECONDS);
    }

    public void offer(TlsConnection connection) {
        ensureConnectionPoolIsInitialized();
        try {
            queue.offer(connection, TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public TlsConnection borrow() {
        ensureConnectionPoolIsInitialized();
        try {
            var connection = queue.poll(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            return connection.reconnectIfUnHealthy();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * Factory method
     */
    public TlsConnection createNewConnection() {
        return TlsConnection.create(config.host(), config.port());
    }

    /**
     * Ensure pattern / Fail fast
     */
    private void ensureConnectionPoolIsInitialized() {
        if (!initialized.get()) {
            throw new IllegalStateException("Connection pool is not initialized");
        }
    }
}
