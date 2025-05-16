package com.github.mangila.pokedex.shared.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BoundedQueueService {

    private static final Logger log = LoggerFactory.getLogger(BoundedQueueService.class);
    private static final BoundedQueueService INSTANCE = new BoundedQueueService();

    private final Map<String, BoundedQueue> boundedQueues;

    public static BoundedQueueService getInstance() {
        return INSTANCE;
    }

    private BoundedQueueService() {
        log.info("Create new bounded queue service");
        this.boundedQueues = new ConcurrentHashMap<>();
    }

    public BoundedQueue createNewBoundedQueue(String queueName, int capacity) {
        log.debug("Create new bounded queue {}", queueName);
        boundedQueues.put(queueName, new BoundedQueue(capacity));
        return boundedQueues.get(queueName);
    }

    public BoundedQueue getBoundedQueue(String queueName) {
        return boundedQueues.get(queueName);
    }

    public boolean add(String queueName, QueueEntry entry) throws InterruptedException {
        log.debug("Add queueEntry to {} - {}", queueName, entry);
        return boundedQueues.get(queueName).add(entry);
    }

    public Optional<QueueEntry> poll(String queueName, Duration timeout) throws InterruptedException {
        log.debug("Poll queueEntry from {} - {}", queueName, timeout);
        var queue = boundedQueues.get(queueName);
        if (queue == null) {
            throw new QueueNotFoundException(queueName);
        }
        return Optional.ofNullable(queue.poll(timeout));
    }

    public QueueEntry take(String queueName) throws InterruptedException {
        log.debug("Take queueEntry from {}", queueName);
        var queue = boundedQueues.get(queueName);
        if (queue == null) {
            throw new QueueNotFoundException(queueName);
        }
        return queue.take();
    }

    public boolean isEmpty(String name) {
        return boundedQueues.get(name).isEmpty();
    }

    public int available(String name) {
        return boundedQueues.get(name).available();
    }

    public void clear(String name) {
        boundedQueues.get(name).clear();
    }

    public Iterator<QueueEntry> iterator(String name) {
        return boundedQueues.get(name).iterator();
    }
}
