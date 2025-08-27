package com.github.mangila.pokedex.shared.queue;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingQueue implements Queue {
    private final QueueName name;
    private final LinkedBlockingQueue<QueueEntry> queue;
    private final LinkedBlockingQueue<QueueEntry> dlq;

    public BlockingQueue(QueueName name) {
        this.name = name;
        this.queue = new LinkedBlockingQueue<>();
        this.dlq = new LinkedBlockingQueue<>();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty() && dlq.isEmpty();
    }

    @Override
    public boolean add(QueueEntry entry) {
        return queue.add(entry);
    }

    @Override
    public @Nullable QueueEntry poll() {
        return queue.poll();
    }

    public @Nullable QueueEntry poll(Duration timeout) throws InterruptedException {
        return queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean addDlq(QueueEntry queueEntry) {
        return dlq.add(queueEntry);
    }

}
