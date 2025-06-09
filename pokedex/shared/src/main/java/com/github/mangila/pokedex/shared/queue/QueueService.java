package com.github.mangila.pokedex.shared.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * BillPugh Singleton implementation that holds queues in a ConcurrentHashMap
 */
public class QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueService.class);

    private final Map<String, ConcurrentLinkedQueue<QueueEntry>> queues;

    private static final class Holder {
        private static final QueueService INSTANCE = new QueueService();
    }

    public static QueueService getInstance() {
        return Holder.INSTANCE;
    }

    private QueueService() {
        log.info("Initializing QueueService");
        this.queues = new ConcurrentHashMap<>();
    }

    public void createNewQueue(String queueName) {
        log.info("Create new queue '{}'", queueName);
        queues.put(queueName, new ConcurrentLinkedQueue<>());
    }

    public boolean add(String queueName, QueueEntry entry) {
        log.debug("Add queueEntry to {} - {}", queueName, entry);
        return queues.get(queueName).add(entry);
    }

    public Optional<QueueEntry> poll(String queueName) {
        log.debug("Poll queueEntry from {}", queueName);
        var queue = queues.get(queueName);
        if (queue == null) {
            throw new QueueNotFoundException(queueName);
        }
        return Optional.ofNullable(queue.poll());
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
