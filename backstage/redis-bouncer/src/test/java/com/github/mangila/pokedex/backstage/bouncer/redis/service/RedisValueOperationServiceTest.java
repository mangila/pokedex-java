package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Disabled(value = "Run only where a Docker env is available")
public class RedisValueOperationServiceTest extends RedisTestContainer {

    @Test
    void set() {
    }

    @Test
    void get() {
    }
}