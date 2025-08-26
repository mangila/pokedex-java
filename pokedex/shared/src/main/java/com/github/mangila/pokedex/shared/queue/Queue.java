package com.github.mangila.pokedex.shared.queue;

import org.jspecify.annotations.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Queue {
    private final QueueName name;
    private final ConcurrentLinkedQueue<QueueEntry> queue;
    private final ConcurrentLinkedQueue<QueueEntry> dlq;

    public Queue(QueueName name) {
        this.name = name;
        this.queue = new ConcurrentLinkedQueue<>();
        this.dlq = new ConcurrentLinkedQueue<>();
    }

    public boolean isEmpty() {
        return queue.isEmpty() && dlq.isEmpty();
    }

    public boolean add(QueueEntry entry) {
        return queue.add(entry);
    }

    public @Nullable QueueEntry poll() {
        return queue.poll();
    }
    public boolean addDlq(QueueEntry queueEntry) {
        return dlq.add(queueEntry);
    }
}
