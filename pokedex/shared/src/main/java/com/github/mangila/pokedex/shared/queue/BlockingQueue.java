package com.github.mangila.pokedex.shared.queue;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingQueue implements Queue {
    private final QueueName name;
    private final LinkedBlockingQueue<QueueEntry> queue;
    private final LinkedBlockingQueue<QueueEntry> dlq;

    public BlockingQueue(QueueName name, int capacity) {
        this.name = name;
        if (capacity > 0) {
            this.queue = new LinkedBlockingQueue<>(capacity);
            this.dlq = new LinkedBlockingQueue<>(capacity);
        } else {
            this.queue = new LinkedBlockingQueue<>();
            this.dlq = new LinkedBlockingQueue<>();
        }
    }

    public BlockingQueue(QueueName name) {
        this(name, 0);
    }

    @Override
    public QueueName name() {
        return name;
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean isDlqEmpty() {
        return dlq.isEmpty();
    }

    @Override
    public boolean add(QueueEntry entry) {
        return queue.add(entry);
    }

    public boolean offer(QueueEntry entry) {
        return queue.offer(entry);
    }

    @Override
    public Iterator<QueueEntry> queueIterator() {
        return queue.iterator();
    }

    @Override
    public @Nullable QueueEntry poll() {
        return queue.poll();
    }

    public @Nullable QueueEntry poll(Duration timeout) throws InterruptedException {
        return queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    public QueueEntry take() throws InterruptedException {
        return queue.take();
    }

    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public boolean addDlq(QueueEntry queueEntry) {
        return dlq.add(queueEntry);
    }

    @Override
    public void clear() {
        queue.clear();
        dlq.clear();
    }

}
