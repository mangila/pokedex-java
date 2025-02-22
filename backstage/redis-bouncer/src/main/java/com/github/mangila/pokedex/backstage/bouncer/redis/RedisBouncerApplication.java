package com.github.mangila.pokedex.backstage.bouncer.redis;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.github.mangila.pokedex.backstage"})
public class RedisBouncerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisBouncerApplication.class, args);
    }
}
