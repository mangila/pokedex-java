package com.github.mangila.pokedex.shared.https.tls;

import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            offerNewConnection();
        }
    }

    public void offerNewConnection() {
        TlsConnectionHandle handler = TlsConnectionHandle.create(config.host(), config.port());
        offer(handler);
    }

    public void offer(TlsConnectionHandle tlsConnectionHandle) {
        if (!queue.offer(QueueEntry.of(tlsConnectionHandle))) {
            LOGGER.warn("Queue is full, dropping connection");
            if (tlsConnectionHandle.isConnected()) {
                tlsConnectionHandle.disconnect();
            }
        } else {
            LOGGER.debug("Connection brought back to the pool");
        }
    }

    public TlsConnectionHandle borrow() throws InterruptedException {
        return queue.take()
                .unwrapAs(TlsConnectionHandle.class)
                .reconnectIfUnHealthy();
    }

    @Override
    public void close() {
        LOGGER.info("Closing connection pool");
        queue.queueIterator()
                .forEachRemaining(queueEntry -> queueEntry.unwrapAs(TlsConnectionHandle.class)
                .disconnect());
        queue.clear();
    }
}
