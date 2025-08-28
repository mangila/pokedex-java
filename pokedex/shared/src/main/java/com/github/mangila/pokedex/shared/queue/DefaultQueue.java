package com.github.mangila.pokedex.shared.queue;

import org.jspecify.annotations.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DefaultQueue implements Queue {
    private final QueueName name;
    private final ConcurrentLinkedQueue<QueueEntry> queue;
    private final ConcurrentLinkedQueue<QueueEntry> dlq;

    public DefaultQueue(QueueName name) {
        this.name = name;
        this.queue = new ConcurrentLinkedQueue<>();
        this.dlq = new ConcurrentLinkedQueue<>();
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

    @Override
    public @Nullable QueueEntry poll() {
        return queue.poll();
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
