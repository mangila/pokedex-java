package com.github.mangila.pokedex.scheduler.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@lombok.AllArgsConstructor
@lombok.extern.slf4j.Slf4j
public class QueueService {

    public static final String REDIS_POKEMON_SET = "pokemon-result-set";
    public static final String REDIS_POKEMON_MEDIA_SET = "pokemon-media-set";

    private final RedisTemplate<String, Object> redisTemplate;

    public <T> Optional<Long> add(String queueName, T entry) {
        var added = redisTemplate
                .opsForSet()
                .add(queueName, entry);
        return Optional.ofNullable(added);
    }

    public <T> void poll(String queueName, Class<T> clazz, Consumer<T> processor) {
        var entry = redisTemplate.opsForSet().pop(queueName);
        if (Objects.nonNull(entry)) {
            try {
                processor.accept(clazz.cast(entry));
            } catch (Exception e) {
                log.error("Failed to process entry", e);
                redisTemplate.opsForSet().add(queueName, entry);
            }
        }
    }

    public boolean isEmpty(String queueName) {
        var size = redisTemplate.opsForSet().size(queueName);
        log.debug("Queue: {} size: {}", queueName, size);
        return Objects.nonNull(size) && size == 0L;
    }

}
