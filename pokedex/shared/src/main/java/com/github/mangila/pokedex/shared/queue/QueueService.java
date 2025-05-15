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

    public void createNewSetQueue(String queueName, int capacity) {
        log.debug("Create new set queue {}, {}", queueName, capacity);
        SET_QUEUES.put(queueName, new LinkedHashSet<>(capacity));
    }

    public boolean push(String queueName, QueueEntry entry) {
        return SET_QUEUES.get(queueName).add(entry);
    }

    public Optional<QueueEntry> poll(String queueName) {
        var queue = SET_QUEUES.get(queueName);
        if (queue == null) {
            return Optional.empty();
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
