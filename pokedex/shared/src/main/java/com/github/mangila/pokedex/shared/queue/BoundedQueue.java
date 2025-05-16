package com.github.mangila.pokedex.shared.queue;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BoundedQueue implements Iterable<QueueEntry> {

    private final Bound bound;
    private final LinkedTransferQueue<QueueEntry> queue;

    public BoundedQueue(int capacity) {
        this.bound = new BoundedQueue.Bound(capacity);
        this.queue = new LinkedTransferQueue<>();
    }

    public boolean add(QueueEntry queueEntry) throws InterruptedException {
        queue.add(queueEntry);
        bound.increment();
        return true;
    }

    public QueueEntry poll(Duration timeout) throws InterruptedException {
        var connection = queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (connection != null) {
            bound.decrement();
        }
        return connection;
    }

    public QueueEntry take() throws InterruptedException {
        var connection = queue.take();
        bound.decrement();
        return connection;
    }

    public void clear() {
        queue.clear();
        bound.clear();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int available() {
        return bound.available();
    }

    @Override
    public Iterator<QueueEntry> iterator() {
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

        public int available() {
            return available.get();
        }

        public void clear() {
            semaphore.drainPermits();
            available.set(0);
        }
    }
}
