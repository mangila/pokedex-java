package com.github.mangila.pokedex.shared.queue;

import org.jspecify.annotations.Nullable;

public interface Queue {

    boolean isEmpty();

    boolean isDlqEmpty();

    boolean add(QueueEntry entry);

    @Nullable
    QueueEntry poll();

    boolean addDlq(QueueEntry queueEntry);

    void clear();
}
