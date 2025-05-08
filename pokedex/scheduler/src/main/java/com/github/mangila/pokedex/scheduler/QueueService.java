package com.github.mangila.pokedex.scheduler;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class QueueService {

    private static final Map<String, LinkedHashSet<QueueEntry>> SET_QUEUES = new HashMap<>();

    public void createNewSetQueue(String name, int capacity) {
        SET_QUEUES.put(name, new LinkedHashSet<>(capacity));
    }

    public boolean push(String name, QueueEntry entry) {
        return SET_QUEUES.get(name).add(entry);
    }

    public QueueEntry poll(String name) {
        return SET_QUEUES.get(name)
                .removeFirst();
    }

    public boolean isEmpty(String name) {
        return SET_QUEUES.get(name).isEmpty();
    }
}
