package com.github.mangila.pokedex.backstage.media.task;

import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Random;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Disabled(value = "Run only where a Docker env is available")
class MediaTaskE2eTest {

    private static final String REDIS_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();
    private static final String MONGO_DB_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();
    @Autowired
    private Task pokemonTask;
    @Autowired
    private RedisBouncerClient redisBouncerClient;

    private static GenericContainer<?> redis;
    private static GenericContainer<?> redisBouncer;
    private static MongoDBContainer mongoDb;
    private static GenericContainer<?> mongoDbBouncer;

    static String generateRandomEphemeralPort() {
        int minPort = 30_000;
        int maxPort = 40_000;
        Random random = new Random();
        return String.valueOf(random.nextInt((maxPort - minPort) + 1) + minPort);
    }

    @BeforeAll
    static void beforeAll() {
        redis = TestContainerUtil.buildRedis();
        redisBouncer = TestContainerUtil.buildRedisBouncer(REDIS_BOUNCER_GRPC_PORT);
        mongoDb = TestContainerUtil.buildMongoDb();
        mongoDbBouncer = TestContainerUtil.buildMongoDbBouncer(MONGO_DB_BOUNCER_GRPC_PORT);
        redis.start();
        redisBouncer.start();
        mongoDb.start();
        mongoDbBouncer.start();
    }

    @AfterAll
    static void afterAll() {
        redis.stop();
        redisBouncer.stop();
        mongoDb.stop();
        mongoDbBouncer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        var redisBouncerAddress = "static://0.0.0.0:".concat(REDIS_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.redis-bouncer.address", () -> redisBouncerAddress);
        var mongoDbBouncerAddress = "static://0.0.0.0:".concat(MONGO_DB_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.mongodb-bouncer.address", () -> mongoDbBouncerAddress);
    }

    @Test
    void test() {

    }
}