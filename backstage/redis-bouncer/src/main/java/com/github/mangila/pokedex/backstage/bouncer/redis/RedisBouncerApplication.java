package com.github.mangila.pokedex.backstage.bouncer.redis;

import com.github.mangila.pokedex.backstage.shared.model.domain.RedisConsumerGroup;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootApplication
@ComponentScan({"com.github.mangila.pokedex.backstage"})
public class RedisBouncerApplication {

    private static final Logger log = LoggerFactory.getLogger(RedisBouncerApplication.class);
    private final StringRedisTemplate template;

    public RedisBouncerApplication(StringRedisTemplate template) {
        this.template = template;
    }

    public static void main(String[] args) {
        SpringApplication.run(RedisBouncerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            template.execute(RedisConnection::ping);
            var group = RedisConsumerGroup.POKEDEX_BACKSTAGE_GROUP;
            tryCreateGroup(RedisStreamKey.POKEMON_NAME_EVENT, group);
            tryCreateGroup(RedisStreamKey.POKEMON_MEDIA_EVENT, group);
        };
    }

    private void tryCreateGroup(RedisStreamKey streamKey, RedisConsumerGroup group) {
        try {
            log.info("Try Create Group: {} with Stream: {}",
                    group.getGroupName(),
                    streamKey.getKey());
            template.opsForStream().createGroup(
                    streamKey.getKey(),
                    group.getGroupName()
            );
        } catch (RedisSystemException e) {
            log.info("Group already exist!");
        }
    }
}
