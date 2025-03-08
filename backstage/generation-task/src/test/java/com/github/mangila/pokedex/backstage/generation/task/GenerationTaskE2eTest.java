package com.github.mangila.pokedex.backstage.generation.task;

import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.GenerationResponsePrototype;
import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.Generation;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.EnumSet;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Disabled(value = "Run only where a Docker env is available - redis-bouncer server needs to be in a Container")
class GenerationTaskE2eTest {

    private static final String POKE_API_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();
    private static final String REDIS_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();

    @Autowired
    private RedisBouncerClient redisBouncerClient;

    static String generateRandomEphemeralPort() {
        int minPort = 49152;
        int maxPort = 65535;
        Random random = new Random();
        return String.valueOf(random.nextInt((maxPort - minPort) + 1) + minPort);
    }

    @BeforeAll
    static void beforeAll() {
        TestContainerUtil.buildRedis().start();
        TestContainerUtil.buildPokeApiBouncer(POKE_API_BOUNCER_GRPC_PORT)
                .start();
        TestContainerUtil.buildRedisBouncer(REDIS_BOUNCER_GRPC_PORT)
                .start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        var grpcPokeApiBouncerAddress = "static://0.0.0.0:".concat(POKE_API_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.redis-bouncer.address", () -> grpcPokeApiBouncerAddress);
        var grpcRedisBouncerAddress = "static://0.0.0.0:".concat(REDIS_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.redis-bouncer.address", () -> grpcRedisBouncerAddress);
    }

    @Test
    void testCachedApiResponses() {
        EnumSet.allOf(Generation.class)
                .stream()
                .map(Generation::getName)
                .forEach(generationName -> {
                    var request = EntryRequest.newBuilder()
                            .setKey(generationName)
                            .build();
                    var response = redisBouncerClient.valueOps()
                            .get(request, GenerationResponsePrototype.class);
                    assertThat(response).isNotEmpty();
                });
    }

    @Test
    void testReadMessage() {
        var readOne = redisBouncerClient.streamOps()
                .readOne(
                        StreamRecord.newBuilder()
                                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                                .build()
                );
        assertThat(readOne.getDataMap())
                .hasSize(1)
                .containsKey("name");
    }
}