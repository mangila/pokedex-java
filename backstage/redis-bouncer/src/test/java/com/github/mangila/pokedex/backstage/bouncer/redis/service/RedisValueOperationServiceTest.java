package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@Disabled(value = "Run only where a Docker env is available")
class RedisValueOperationServiceTest {

    private static final DockerImageName REDIS_CONTAINER_NAME = DockerImageName.parse("redis:7.4.2-alpine");

    @SuppressWarnings("rawtypes")
    @ServiceConnection
    public static final GenericContainer REDIS = new GenericContainer(REDIS_CONTAINER_NAME)
            .withExposedPorts(6379);

    @Test
    void set() {
    }

    @Test
    void get() {
    }
}