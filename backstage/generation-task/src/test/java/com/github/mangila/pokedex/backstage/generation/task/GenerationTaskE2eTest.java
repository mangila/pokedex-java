package com.github.mangila.pokedex.backstage.generation.task;

import com.github.mangila.pokedex.backstage.model.grpc.model.GenerationResponse;
import com.github.mangila.pokedex.backstage.model.grpc.model.StreamRecord;
import com.github.mangila.pokedex.backstage.model.grpc.model.ValueRequest;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.Generation;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisKeyPrefix;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.EnumSet;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Disabled(value = "Run only where a Docker env is available - redis and pokeapi bouncer needs to be in container")
class GenerationTaskE2eTest {

    private static final String POKE_API_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();
    private static final String REDIS_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();

    @Autowired
    private RedisBouncerClient redisBouncerClient;
    @Autowired
    private Task generationTask;

    private static GenericContainer<?> pokeApiBouncer;
    private static GenericContainer<?> redis;
    private static GenericContainer<?> redisBouncer;

    static String generateRandomEphemeralPort() {
        int minPort = 30_000;
        int maxPort = 40_000;
        Random random = new Random();
        return String.valueOf(random.nextInt((maxPort - minPort) + 1) + minPort);
    }

    @BeforeAll
    static void beforeAll() {
        pokeApiBouncer = TestContainerUtil.buildPokeApiBouncer(POKE_API_BOUNCER_GRPC_PORT, REDIS_BOUNCER_GRPC_PORT);
        redis = TestContainerUtil.buildRedis();
        redisBouncer = TestContainerUtil.buildRedisBouncer(REDIS_BOUNCER_GRPC_PORT);
        pokeApiBouncer.start();
        redis.start();
        redisBouncer.start();
    }

    @AfterAll
    static void afterAll() {
        pokeApiBouncer.stop();
        redis.stop();
        redisBouncer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        var grpcPokeApiBouncerAddress = "static://0.0.0.0:".concat(POKE_API_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.pokeapi-bouncer.address", () -> grpcPokeApiBouncerAddress);
        var grpcRedisBouncerAddress = "static://0.0.0.0:".concat(REDIS_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.redis-bouncer.address", () -> grpcRedisBouncerAddress);
    }

    @Test
    void shouldCachePokeApiResponses() {
        assertThatCode(() -> generationTask.run(new String[0])).doesNotThrowAnyException();
        EnumSet.allOf(Generation.class)
                .stream()
                .map(Generation::getName)
                .forEach(generationName -> {
                    var request = ValueRequest.newBuilder()
                            .setKey(RedisKeyPrefix.GENERATION_KEY_PREFIX.getPrefix().concat(generationName))
                            .build();
                    var response = redisBouncerClient.valueOps()
                            .get(request, GenerationResponse.class);
                    assertThat(response).isNotEmpty();
                });
    }

    @Test
    void shouldAddStreamMessages() {
        assertThatCode(() -> generationTask.run(new String[0])).doesNotThrowAnyException();
        var record = StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .build();
        var streamRecord = redisBouncerClient.streamOps().readOne(record);
        assertThat(streamRecord.getDataMap())
                .hasSize(1)
                .containsKey("name");
    }
}