package com.github.mangila.pokedex.backstage.bouncer.redis;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootApplication
@ComponentScan({"com.github.mangila.pokedex.backstage"})
public class RedisBouncerApplication {

    private final StringRedisTemplate template;

    public RedisBouncerApplication(StringRedisTemplate template) {
        this.template = template;
    }

    public static void main(String[] args) {
        SpringApplication.run(RedisBouncerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> template.execute(RedisConnectionCommands::ping);
    }
}
