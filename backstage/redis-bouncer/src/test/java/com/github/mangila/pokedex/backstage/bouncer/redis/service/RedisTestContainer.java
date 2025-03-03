package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class RedisTestContainer {

    private static final DockerImageName REDIS_CONTAINER_NAME = DockerImageName.parse("redis:7.4.2-alpine");

    @SuppressWarnings("rawtypes")
    @ServiceConnection
    public static final GenericContainer REDIS = new GenericContainer(REDIS_CONTAINER_NAME)
            .withExposedPorts(6379);
}
