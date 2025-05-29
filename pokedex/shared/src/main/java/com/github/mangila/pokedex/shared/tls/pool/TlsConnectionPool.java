package com.github.mangila.pokedex.shared.tls.pool;

import com.github.mangila.pokedex.shared.config.VirtualThreadConfig;
import com.github.mangila.pokedex.shared.tls.TlsConnection;
import com.github.mangila.pokedex.shared.tls.config.TlsConnectionPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <summary>
 * Bounded Connection Pool using Semaphore
 * </summary>
 */
public class TlsConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(TlsConnectionPool.class);

    private final TlsConnectionPoolConfig config;
    private final int maxConnections;
    private final ConcurrentLinkedQueue<TlsConnection> queue;
    private final Semaphore bound;
    private final HealthProbe healthProbe;
    private final AtomicBoolean initialized;

    public TlsConnectionPool(TlsConnectionPoolConfig config) {
        this.config = config;
        this.maxConnections = config.maxConnections();
        this.queue = new ConcurrentLinkedQueue<>();
        this.bound = new Semaphore(maxConnections, Boolean.TRUE);
        this.healthProbe = new HealthProbe(this.queue);
        this.initialized = new AtomicBoolean(Boolean.FALSE);
    }

    public void init() {
        initialized.set(Boolean.TRUE);
        for (int i = 0; i < maxConnections; i++) {
            log.debug("Creating new connection - {} of {}", i + 1, maxConnections);
            var connection = createNewConnection();
            queue.offer(connection);
        }
        VirtualThreadConfig.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(healthProbe,
                        config.healthCheckConfig().initialDelay(),
                        config.healthCheckConfig().delay(),
                        config.healthCheckConfig().timeUnit());
    }

    /**
     * <summary>
     * Offer connection and release a permit if not exceed maxConnections
     * safety for semaphore permits missmatch
     * </summary>
     */
    public void offer(TlsConnection connection) {
        ensureConnectionPoolIsInitialized();
        queue.offer(connection);
        if (bound.availablePermits() < maxConnections) {
            bound.release();
        }
    }

    /**
     * <summary>
     * Acquire permit and poll queue. If null returns we have a Semaphore permit missmatch
     * </summary>
     */
    public TlsConnection borrow() {
        ensureConnectionPoolIsInitialized();
        bound.acquireUninterruptibly();
        var connection = queue.poll();
        if (connection == null) {
            throw new IllegalStateException("Connection pool is empty");
        }
        return connection.reconnectIfUnHealthy();
    }

    public int availablePermits() {
        return bound.availablePermits();
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
