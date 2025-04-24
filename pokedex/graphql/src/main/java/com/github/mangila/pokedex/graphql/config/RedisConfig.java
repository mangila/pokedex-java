package com.github.mangila.pokedex.graphql.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @PostConstruct
    public void init() {
        logger.info("Redis configuration: host={}, port={}", redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        logger.debug("Creating RedisTemplate bean with connection factory: {}", redisConnectionFactory);
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(redisConnectionFactory);

        logger.debug("Configuring RedisTemplate serializers");
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.json());

        logger.info("RedisTemplate bean configured successfully");
        return template;
    }
}
