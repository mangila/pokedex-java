package com.github.mangila.pokedex.scheduler.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@lombok.extern.slf4j.Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @PostConstruct
    public void init() {
        log.info("Redis: {} - {}", redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        var t = new RedisTemplate<String, Object>();
        t.setConnectionFactory(redisConnectionFactory);
        t.setKeySerializer(RedisSerializer.string());
        t.setValueSerializer(RedisSerializer.json());
        return t;
    }
}