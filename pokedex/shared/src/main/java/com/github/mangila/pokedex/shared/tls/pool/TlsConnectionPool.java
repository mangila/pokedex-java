package com.github.mangila.pokedex.shared.tls.pool;

import com.github.mangila.pokedex.shared.tls.TlsConnectionHandler;
import com.github.mangila.pokedex.shared.util.Ensure;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * <summary>
 * Bounded Connection Pool using Semaphore
 * </summary>
 */
public class TlsConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(TlsConnectionPool.class);

    private final TlsConnectionPoolConfig config;
    private final int maxConnections;
    private final ArrayBlockingQueue<TlsConnectionHandler> queue;

    private final AtomicInteger availableConnections;
    private volatile boolean initialized;

    public TlsConnectionPool(TlsConnectionPoolConfig config) {
        this.config = config;
        this.maxConnections = config.maxConnections();
        this.availableConnections = new AtomicInteger(0);
        this.queue = new ArrayBlockingQueue<>(maxConnections, false);
        this.initialized = false;
    }

    public void init() {
        this.initialized = true;
        IntStream.range(1, maxConnections + 1)
                .peek(value -> log.debug("Creating new connection - {} of {}", value, maxConnections))
                .forEach(unused -> offerNewConnection());
    }

    public void offer(TlsConnectionHandler tlsConnectionHandler) {
        ensureConnectionPoolIsInitialized();
        if (!queue.offer(tlsConnectionHandler)) {
            log.warn("Queue is full, dropping tlsConnectionHandler");
            tlsConnectionHandler.disconnect();
        } else {
            log.debug("Connection offered to queue");
            tlsConnectionHandler.reconnectIfUnHealthy();
            availableConnections.incrementAndGet();
        }
    }

    public @Nullable TlsConnectionHandler borrow(Duration timeout) throws InterruptedException {
        ensureConnectionPoolIsInitialized();
        TlsConnectionHandler tlsConnectionHandler = queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (tlsConnectionHandler == null) {
            log.debug("Timeout waiting for tlsConnectionHandler");
            return null;
        }
        availableConnections.decrementAndGet();
        return tlsConnectionHandler.reconnectIfUnHealthy();
    }

    public void offerNewConnection() {
        ensureConnectionPoolIsInitialized();
        TlsConnectionHandler handler = TlsConnectionHandler.create(config.host(), config.port());
        offer(handler);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public int availableConnections() {
        return availableConnections.get();
    }

    public void close() {
        initialized = false;
        queue.forEach(TlsConnectionHandler::disconnect);
        queue.clear();
        availableConnections.set(0);
    }

    private void ensureConnectionPoolIsInitialized() {
        Ensure.isTrue(isInitialized(), "Connection pool is not initialized");
    }
}
