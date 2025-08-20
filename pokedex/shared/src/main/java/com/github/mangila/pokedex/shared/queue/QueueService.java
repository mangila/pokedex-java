package com.github.mangila.pokedex.shared.queue;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueService.class);
    private final Map<String, ConcurrentLinkedQueue<QueueEntry>> queues;

    private QueueService() {
        LOGGER.info("Initializing QueueService");
        this.queues = new ConcurrentHashMap<>();
    }

    private static final class Holder {
        private static final QueueService INSTANCE = new QueueService();
    }

    public static QueueService getInstance() {
        return Holder.INSTANCE;
    }

    public void createNewQueue(String queueName) {
        LOGGER.info("Create new queue '{}'", queueName);
        queues.put(queueName, new ConcurrentLinkedQueue<>());
    }

    public boolean add(String queueName, QueueEntry entry) {
        LOGGER.debug("Add QueueEntry to {} - {}", queueName, entry);
        return queues.get(queueName).add(entry);
    }

    public @Nullable QueueEntry poll(String queueName) {
        LOGGER.debug("Poll QueueEntry from {}", queueName);
        var queue = queues.get(queueName);
        if (queue == null) {
            throw new QueueNotFoundException(queueName);
        }
        return queue.poll();
    }

    public boolean isEmpty(String name) {
        return queues.get(name).isEmpty();
    }

    public boolean allQueuesEmpty() {
        return queues.values()
                .stream()
                .allMatch(ConcurrentLinkedQueue::isEmpty);
    }
}
