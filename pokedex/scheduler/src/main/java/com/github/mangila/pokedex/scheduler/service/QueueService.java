package com.github.mangila.pokedex.scheduler.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@lombok.AllArgsConstructor
@lombok.extern.slf4j.Slf4j
public class QueueService {

    public static final String POKEMON_QUEUE = "pokemon-queue";
    public static final String MEDIA_QUEUE = "media-queue";

    private final RedisTemplate<String, Object> redisTemplate;

    public <T> Optional<Long> add(String queueName, T entry) {
        var added = redisTemplate
                .opsForSet()
                .add(queueName, entry);
        return Optional.ofNullable(added);
    }

    public <T> Optional<T> poll(String queueName, Class<T> clazz) {
        var entry = redisTemplate.opsForSet().pop(queueName);
        return Optional.ofNullable(clazz.cast(entry));
    }

    public boolean isEmpty(String queueName) {
        var size = redisTemplate.opsForSet().size(queueName);
        log.debug("Queue: {} size: {}", queueName, size);
        return Objects.nonNull(size) && size == 0L;
    }
}
