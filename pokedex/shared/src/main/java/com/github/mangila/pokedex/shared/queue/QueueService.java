package com.github.mangila.pokedex.shared.queue;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

public class QueueService {

    private static final Map<String, LinkedHashSet<QueueEntry>> SET_QUEUES = new HashMap<>();

    public void createNewSetQueue(String name, int capacity) {
        SET_QUEUES.put(name, new LinkedHashSet<>(capacity));
    }

    public boolean push(String name, QueueEntry entry) {
        return SET_QUEUES.get(name).add(entry);
    }

    public Optional<QueueEntry> poll(String name) {
        var queue = SET_QUEUES.get(name);
        if (queue == null) {
            throw new IllegalArgumentException("Queue not found");
        }
        try {
            return Optional.of(queue.removeFirst());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isEmpty(String name) {
        return SET_QUEUES.get(name).isEmpty();
    }
}
