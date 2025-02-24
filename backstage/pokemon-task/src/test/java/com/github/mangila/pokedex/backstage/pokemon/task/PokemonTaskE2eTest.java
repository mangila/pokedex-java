package com.github.mangila.pokedex.backstage.pokemon.task;

import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Disabled(value = "Run only where a Docker env is available")
class PokemonTaskE2eTest {

    private static final String GRPC_PORT = "9999";

    @Autowired
    private RedisBouncerClient redisBouncerClient;

    private static final DockerImageName REDIS_CONTAINER_NAME = DockerImageName.parse("redis:7.4.2-alpine");
    private static final DockerImageName REDIS_BOUNCER_CONTAINER_NAME = DockerImageName.parse("mangila/pokedex-redis-bouncer");

    public static GenericContainer redis;
    public static GenericContainer redisBouncer;

    @BeforeAll
    static void beforeAll() {
        redis = new GenericContainer(REDIS_CONTAINER_NAME)
                .withNetworkAliases("redis")
                .withNetwork(Network.SHARED);
        redis.start();
        redisBouncer = new GenericContainer(REDIS_BOUNCER_CONTAINER_NAME)
                .withNetwork(Network.SHARED)
                .withEnv("spring.grpc.server.port", GRPC_PORT)
                .withEnv("spring.data.redis.host", "redis")
                .withEnv("spring.data.redis.port", "6379")
                .waitingFor(new LogMessageWaitStrategy()
                        .withRegEx(".*gRPC Server started.*")
                        .withTimes(1)
                        .withStartupTimeout(Duration.ofSeconds(5)));
        redisBouncer.setPortBindings(List.of(
                GRPC_PORT.concat(":").concat(GRPC_PORT) //host:container port
        ));
        redisBouncer.start();
    }

    @AfterAll
    static void afterAll() {
        redis.stop();
        redis.close();
        redisBouncer.stop();
        redisBouncer.close();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        var grpcAddress = "static://0.0.0.0:".concat(GRPC_PORT);
        registry.add("spring.grpc.client.channels.redis-bouncer.address", () -> grpcAddress);
    }

    @Test
    void test() {

    }
}