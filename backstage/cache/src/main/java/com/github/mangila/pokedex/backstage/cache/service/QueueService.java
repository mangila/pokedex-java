package com.github.mangila.pokedex.backstage.cache.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

    private final RedisTemplate<String, Object> redisObjectTemplate;
    private final RedisTemplate<String, String> redisStringTemplate;

    public QueueService(RedisTemplate<String, Object> redisObjectTemplate,
                        RedisTemplate<String, String> redisStringTemplate) {
        this.redisObjectTemplate = redisObjectTemplate;
        this.redisStringTemplate = redisStringTemplate;
    }

    public void add(String queueName, Object value) {
        redisObjectTemplate.opsForSet().add(queueName, value);
    }

    public void add(String queueName, String value) {
        redisStringTemplate.opsForSet().add(queueName, value);
    }

    public Object pop(String queueName) {
        return redisObjectTemplate.opsForSet().pop(queueName);
    }

    public String popAsString(String queueName) {
        return redisStringTemplate.opsForSet().pop(queueName);
    }
}
