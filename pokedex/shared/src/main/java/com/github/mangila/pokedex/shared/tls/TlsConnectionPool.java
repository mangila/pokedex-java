package com.github.mangila.pokedex.shared.tls;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class TlsConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(TlsConnectionPool.class);

    private final TlsConnectionPoolConfig config;
    private final int maxConnections;
    private final BlockingQueue<TlsConnectionHandler> queue;
    private final AtomicInteger availableConnections;
    private volatile boolean initialized;

    public TlsConnectionPool(TlsConnectionPoolConfig config) {
        this.config = config;
        this.maxConnections = config.maxConnections();
        this.availableConnections = new AtomicInteger(0);
        this.queue = new ArrayBlockingQueue<>(maxConnections, true);
        this.initialized = false;
    }

    public void offer(TlsConnectionHandler tlsConnectionHandler) {
        if (!queue.offer(tlsConnectionHandler)) {
            LOGGER.warn("Queue is full, dropping tlsConnectionHandler");
            if (tlsConnectionHandler.connected()) {
                tlsConnectionHandler.disconnect();
            }
        } else {
            tlsConnectionHandler.reconnectIfUnHealthy();
            availableConnections.incrementAndGet();
            LOGGER.debug("Connection brought back to the pool - availableConnections = {}", availableConnections.get());
        }
    }

    public @Nullable TlsConnectionHandler borrowWithRetry(Duration timeout, int attempts) {
        TlsConnectionHandler tlsConnectionHandler;
        int retries = attempts;
        do {
            tlsConnectionHandler = borrow(timeout);
            retries--;
        } while (retries > 0 && tlsConnectionHandler == null);
        return tlsConnectionHandler;
    }

    public @Nullable TlsConnectionHandler borrow(Duration timeout) {
        if (!initialized) {
            init();
        }
        return poll(timeout);
    }

    public void offerNewConnection() {
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

    private void init() {
        this.initialized = true;
        IntStream.range(1, maxConnections + 1)
                .peek(value -> LOGGER.debug("Creating new connection - {} of {}", value, maxConnections))
                .forEach(unused -> offerNewConnection());
    }

    private @Nullable TlsConnectionHandler poll(Duration timeout) {
        try {
            TlsConnectionHandler tlsConnectionHandler = queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (tlsConnectionHandler == null) {
                LOGGER.debug("No connection available after timeout - {}", timeout);
                return null;
            }
            availableConnections.decrementAndGet();
            LOGGER.debug("Connection borrowed from the pool - availableConnections = {}", availableConnections.get());
            return tlsConnectionHandler.reconnectIfUnHealthy();
        } catch (InterruptedException e) {
            LOGGER.error("ERR", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
