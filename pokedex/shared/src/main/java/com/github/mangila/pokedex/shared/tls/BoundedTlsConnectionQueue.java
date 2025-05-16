package com.github.mangila.pokedex.shared.tls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BoundedTlsConnectionQueue implements Iterable<PooledTlsConnection> {

    private static final Logger log = LoggerFactory.getLogger(BoundedTlsConnectionQueue.class);

    private final Bound bound;
    private final LinkedTransferQueue<PooledTlsConnection> queue;

    public BoundedTlsConnectionQueue(int capacity) {
        this.bound = new Bound(capacity);
        this.queue = new LinkedTransferQueue<>();
    }

    public void add(PooledTlsConnection connection) throws InterruptedException {
        log.debug("Adding connection to queue - {}", connection.id());
        queue.add(connection);
        bound.increment();
    }

    public Optional<PooledTlsConnection> poll(Duration timeout) throws InterruptedException {
        log.debug("Poll connection from queue");
        var connection = queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (connection != null) {
            bound.decrement();
        }
        return Optional.ofNullable(connection);
    }

    public PooledTlsConnection take() throws InterruptedException {
        log.debug("Take connection from queue");
        var connection = queue.take();
        bound.decrement();
        return connection;
    }

    public void clear() {
        queue.forEach(PooledTlsConnection::disconnect);
        queue.clear();
        bound.clear();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int availableConnections() {
        return bound.availableConnections();
    }

    @Override
    public Iterator<PooledTlsConnection> iterator() {
        return queue.iterator();
    }

    private static class Bound {

        private final int capacity;
        private final AtomicInteger available;
        private final Semaphore semaphore;

        private Bound(int capacity) {
            this.capacity = capacity;
            semaphore = new Semaphore(capacity, Boolean.TRUE);
            available = new AtomicInteger(0);
        }

        public void decrement() throws InterruptedException {
            semaphore.acquire();
            available.decrementAndGet();
        }

        public void increment() {
            if (available.get() >= capacity) {
                throw new IllegalStateException("Queue is full");
            }
            available.incrementAndGet();
            semaphore.release();
        }

        public int availableConnections() {
            return available.get();
        }

        public void clear() {
            semaphore.drainPermits();
            available.set(0);
        }
    }
}
