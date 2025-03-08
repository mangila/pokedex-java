package com.github.mangila.pokedex.backstage.bouncer.pokeapi.service;

import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.GenerationResponsePrototype;
import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi.PokeApiBouncerClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.Generation;
import com.google.protobuf.StringValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.grpc.server.port=32768"
})
@Testcontainers
@Disabled(value = "Run only where a Docker env is available")
class PokeApiBouncerServiceTest {

    private static final String POKEAPI_BOUNCER_GRPC_PORT = "32768";
    private static final String REDIS_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();

    @Autowired
    private PokeApiBouncerClient pokeApiBouncerClient;
    @Autowired
    private RedisBouncerClient redisBouncerClient;

    static String generateRandomEphemeralPort() {
        int minPort = 49152;
        int maxPort = 65535;
        Random random = new Random();
        return String.valueOf(random.nextInt((maxPort - minPort) + 1) + minPort);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @BeforeAll
    static void beforeAll() {
        TestContainerUtil.buildRedis().start();
        TestContainerUtil.buildRedisBouncer(REDIS_BOUNCER_GRPC_PORT)
                .start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        var grpcRedisBouncerAddress = "static://0.0.0.0:".concat(REDIS_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.redis-bouncer.address", () -> grpcRedisBouncerAddress);
        // Wire the PokeApiBouncerClient to this test
        var grpcPokeApiBouncerAddress = "static://0.0.0.0:".concat(POKEAPI_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.pokeapi-bouncer.address", () -> grpcPokeApiBouncerAddress);
    }

    @Test
    void test() {
        var cache = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .setKey(Generation.GENERATION_I.getName())
                        .build(), GenerationResponsePrototype.class);
        assertThat(cache).isEmpty();
        var response = pokeApiBouncerClient.fetchGeneration(StringValue.newBuilder()
                .setValue(Generation.GENERATION_I.getName())
                .build());
        assertThat(response.getNameList())
                .isNotEmpty()
                .hasSize(1);
        cache = redisBouncerClient.valueOps()
                .get(EntryRequest.newBuilder()
                        .build(), GenerationResponsePrototype.class);
        assertThat(cache).isNotEmpty();
        assertThat(cache.get().getNameList())
                .isNotEmpty()
                .hasSize(1);
    }
}