package com.github.mangila.pokedex.shared.queue;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;

public interface Queue {

    QueueName name();
    boolean isEmpty();

    boolean isDlqEmpty();

    boolean add(QueueEntry entry);

    @Nullable
    QueueEntry poll();

    boolean addDlq(QueueEntry queueEntry);

    void clear();

    Iterator<QueueEntry> queueIterator();
}
