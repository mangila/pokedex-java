package com.github.mangila.pokedex.backstage.bouncer.pokeapi.service;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;

final class TestContainerUtil {

    private TestContainerUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final DockerImageName REDIS_CONTAINER_NAME = DockerImageName.parse("redis:7.4.2-alpine");
    private static final DockerImageName REDIS_BOUNCER_CONTAINER_NAME = DockerImageName.parse("mangila/pokedex-redis-bouncer");

    @SuppressWarnings({"unchecked", "rawtypes"})
    static GenericContainer<?> buildRedis() {
        return new GenericContainer(REDIS_CONTAINER_NAME)
                .withNetworkAliases("redis");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static GenericContainer<?> buildRedisBouncer(String port) {
        var redisBouncer = new GenericContainer(REDIS_BOUNCER_CONTAINER_NAME)
                .withNetwork(Network.SHARED)
                .withEnv("spring.grpc.server.port", port)
                .withEnv("spring.data.redis.host", "redis")
                .withEnv("spring.data.redis.port", "6379")
                .waitingFor(new LogMessageWaitStrategy()
                        .withRegEx(".*gRPC Server started.*")
                        .withTimes(1)
                        .withStartupTimeout(Duration.ofSeconds(1)));
        redisBouncer.setPortBindings(List.of(
                port.concat(":").concat(port) //host:container port
        ));
        return redisBouncer;
    }
}
