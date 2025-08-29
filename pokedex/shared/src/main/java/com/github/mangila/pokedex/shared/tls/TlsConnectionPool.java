package com.github.mangila.pokedex.shared.tls;

import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * HTTP 1.1 connection pool for TLS connections.
 * <p>
 * This is the solution to achieve concurrency with HTTP 1.1.
 * <p>
 * If using HTTP 2.0, a connection pool is not needed.
 * HTTP 2.0 concurrency is handled by the protocol itself.
 */
// TODO: add metrics
public class TlsConnectionPool implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TlsConnectionPool.class);

    private final TlsConnectionPoolConfig config;
    private final BlockingQueue queue;

    public TlsConnectionPool(TlsConnectionPoolConfig config) {
        this.config = config;
        this.queue = config.queue();
        LOGGER.info("Creating new tls connection pool with {} connections", queue.remainingCapacity());
        for (int i = 0; i <= queue.remainingCapacity(); i++) {
            TlsConnectionHandler handler = TlsConnectionHandler.create(config.host(), config.port());
            offer(handler);
        }
    }

    public void offer(TlsConnectionHandler tlsConnectionHandler) {
        if (!queue.offer(QueueEntry.of(tlsConnectionHandler))) {
            LOGGER.warn("Queue is full, dropping connection");
            if (tlsConnectionHandler.connected()) {
                tlsConnectionHandler.disconnect();
            }
        } else {
            LOGGER.debug("Connection brought back to the pool");
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
        return poll(timeout);
    }

    public void offerNewConnection() {
        TlsConnectionHandler handler = TlsConnectionHandler.create(config.host(), config.port());
        offer(handler);
    }

    @Override
    public void close() {
        LOGGER.info("Closing connection pool");
        queue.queueIterator().forEachRemaining(queueEntry -> queueEntry.unwrapAs(TlsConnectionHandler.class)
                .disconnect());
        queue.clear();
    }

    private @Nullable TlsConnectionHandler poll(Duration timeout) {
        try {
            QueueEntry queueEntry = queue.poll(timeout);
            if (queueEntry == null) {
                LOGGER.debug("No connection available after timeout - {}", timeout);
                return null;
            }
            LOGGER.debug("Connection borrowed from the pool");
            return queueEntry.unwrapAs(TlsConnectionHandler.class)
                    .reconnectIfUnHealthy();
        } catch (InterruptedException e) {
            LOGGER.error("ERR", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
