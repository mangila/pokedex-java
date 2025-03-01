package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@Disabled(value = "Run only where a Docker env is available")
class RedisValueOperationServiceTest {

    private static final DockerImageName REDIS_CONTAINER_NAME = DockerImageName.parse("redis:7.4.2-alpine");

    @SuppressWarnings("rawtypes")
    public static GenericContainer redis;

    @SuppressWarnings("rawtypes")
    @BeforeAll
    static void beforeAll() {
        redis = new GenericContainer(REDIS_CONTAINER_NAME)
                .withExposedPorts(6379);
        redis.start();
    }

    @AfterAll
    static void afterAll() {
        redis.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Test
    void set() {
    }

    @Test
    void get() {
    }
}