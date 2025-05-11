package com.github.mangila.pokedex.shared.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

public class QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueService.class);
    private static final Map<String, LinkedHashSet<QueueEntry>> SET_QUEUES = new HashMap<>();

    public void createNewSetQueue(String name, int capacity) {
        log.debug("Create new set queue {}, {}", name, capacity);
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
