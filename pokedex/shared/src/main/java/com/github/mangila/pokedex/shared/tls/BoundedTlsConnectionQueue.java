package com.github.mangila.pokedex.shared.tls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class BoundedTlsConnectionQueue implements Iterable<PooledTlsConnection> {

    private static final Logger log = LoggerFactory.getLogger(BoundedTlsConnectionQueue.class);

    private final Semaphore bound;
    private final LinkedTransferQueue<PooledTlsConnection> queue;

    public BoundedTlsConnectionQueue(int capacity) {
        this.bound = new Semaphore(capacity);
        this.queue = new LinkedTransferQueue<>();
    }

    public boolean add(PooledTlsConnection connection) throws InterruptedException {
        log.debug("Adding connection to queue - {}", connection.id());
        boolean ok = queue.add(connection);
        bound.release();
        return ok;
    }

    public PooledTlsConnection poll(Duration timeout) throws InterruptedException {
        log.debug("Polling connection from queue");
        bound.acquire();
        return queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    public PooledTlsConnection take() throws InterruptedException {
        log.debug("Polling connection from queue");
        bound.acquire();
        return queue.take();
    }

    public void clear() {
        queue.forEach(PooledTlsConnection::disconnect);
        queue.clear();
        bound.drainPermits();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int availableConnections() {
        return bound.availablePermits();
    }

    @Override
    public Iterator<PooledTlsConnection> iterator() {
        return queue.iterator();
    }
}
