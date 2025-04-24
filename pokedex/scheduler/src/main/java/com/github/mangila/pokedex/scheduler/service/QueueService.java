package com.github.mangila.pokedex.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class QueueService {

    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);
    public static final String POKEMON_QUEUE = "pokemon-queue";
    public static final String MEDIA_QUEUE = "media-queue";

    private final RedisTemplate<String, Object> redisTemplate;

    public QueueService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> Optional<Long> add(String queueName, T entry) {
        logger.debug("Adding entry to queue: queue={}, entryType={}", queueName, entry.getClass().getSimpleName());
        var added = redisTemplate
                .opsForSet()
                .add(queueName, entry);
        if (Objects.nonNull(added) && added != 0) {
            logger.info("Successfully added entry to queue: queue={}, entryType={}", queueName, entry.getClass().getSimpleName());
        } else {
            logger.warn("Failed to add entry to queue: queue={}, entryType={}", queueName, entry.getClass().getSimpleName());
        }
        return Optional.ofNullable(added);
    }

    public <T> Optional<T> poll(String queueName, Class<T> clazz) {
        logger.debug("Polling entry from queue: queue={}, expectedType={}", queueName, clazz.getSimpleName());
        var entry = redisTemplate.opsForSet().pop(queueName);
        if (entry != null) {
            logger.info("Successfully polled entry from queue: queue={}, type={}", queueName, entry.getClass().getSimpleName());
            return Optional.of(clazz.cast(entry));
        } else {
            logger.debug("No entries available in queue: {}", queueName);
            return Optional.empty();
        }
    }

    public boolean isEmpty(String queueName) {
        var size = redisTemplate.opsForSet().size(queueName);
        logger.debug("Checking if queue is empty: queue={}, size={}", queueName, size);
        boolean isEmpty = Objects.nonNull(size) && size == 0L;
        if (isEmpty) {
            logger.info("Queue is empty: {}", queueName);
        }
        return isEmpty;
    }
}
