package com.github.mangila.pokedex.database.internal.io.internal.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DiskOperationQueue<T> {

    private final Queue<T> queue;

    public DiskOperationQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public void add(T operation) {
        queue.add(operation);
    }

    public T poll() {
        return queue.poll();
    }
}
