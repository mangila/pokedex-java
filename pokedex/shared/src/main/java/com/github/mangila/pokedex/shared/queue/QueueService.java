package com.github.mangila.pokedex.shared.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueService.class);
    private static final Map<String, ConcurrentLinkedQueue<QueueEntry>> QUEUES = new ConcurrentHashMap<>();

    public void createNewQueue(String queueName) {
        log.debug("Create new queue {}", queueName);
        QUEUES.put(queueName, new ConcurrentLinkedQueue<>());
    }

    public boolean add(String queueName, QueueEntry entry) {
        return QUEUES.get(queueName).add(entry);
    }

    public Optional<QueueEntry> poll(String queueName) {
        var queue = QUEUES.get(queueName);
        if (queue == null) {
            log.warn("Queue - {} not found", queueName);
            return Optional.empty();
        }
        return Optional.ofNullable(queue.poll());
    }

    public boolean isEmpty(String name) {
        return QUEUES.get(name).isEmpty();
    }
}
