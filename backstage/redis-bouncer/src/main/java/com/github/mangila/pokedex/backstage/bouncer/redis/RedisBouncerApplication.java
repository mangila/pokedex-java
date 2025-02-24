package com.github.mangila.pokedex.backstage.bouncer.redis;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
@ComponentScan({"com.github.mangila.pokedex.backstage"})
public class RedisBouncerApplication {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisBouncerApplication(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(RedisBouncerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> redisTemplate.execute(RedisConnectionCommands::ping);
    }
}
