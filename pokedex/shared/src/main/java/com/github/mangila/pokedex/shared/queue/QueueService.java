package com.github.mangila.pokedex.shared.queue;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueService.class);

    private final Map<QueueName, Queue> queues;

    private QueueService() {
        this.queues = new ConcurrentHashMap<>();
    }

    private static final class Holder {
        private static final QueueService INSTANCE = new QueueService();
    }

    public static QueueService getInstance() {
        return Holder.INSTANCE;
    }

    public Queue createNewQueue(QueueName queueName) {
        LOGGER.info("Create new queue '{}'", queueName);
        queues.put(queueName, new DefaultQueue(queueName));
        return queues.get(queueName);
    }

    public BlockingQueue createNewBlockingQueue(QueueName queueName) {
        LOGGER.info("Create new blocking queue '{}'", queueName);
        queues.put(queueName, new BlockingQueue(queueName));
        return (BlockingQueue) queues.get(queueName);
    }

    public Queue getQueue(QueueName queueName) {
        return queues.get(queueName);
    }

    public BlockingQueue getBlockingQueue(QueueName queueName) {
        return (BlockingQueue) queues.get(queueName);
    }

    public boolean add(QueueName queueName, QueueEntry entry) {
        return queues.get(queueName).add(entry);
    }

    public @Nullable QueueEntry poll(QueueName queueName) {
        Queue queue = queues.get(queueName);
        if (queue == null) {
            throw new QueueNotFoundException(queueName);
        }
        return queue.poll();
    }

    public boolean isEmpty(QueueName queueName) {
        return queues.get(queueName).isEmpty();
    }

    public boolean allQueuesEmpty() {
        return queues.values()
                .stream()
                .allMatch(Queue::isEmpty);
    }
}
