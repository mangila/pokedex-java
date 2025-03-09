package com.github.mangila.pokedex.backstage.bouncer.pokeapi.service;

import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.GenerationResponsePrototype;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.PokeApiGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.PokemonResponsePrototype;
import com.github.mangila.pokedex.backstage.model.grpc.pokeapi.PokemonSpeciesResponsePrototype;
import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.Generation;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisKeyPrefix;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.grpc.server.port=32768"
})
@Testcontainers
@Disabled(value = "Run only where a Docker env is available")
class PokeApiBouncerServiceTest {

    private static final String REDIS_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();
    private static final ManagedChannel MANAGED_CHANNEL = ManagedChannelBuilder.forAddress("0.0.0.0", 32768)
            .usePlaintext()
            .maxInboundMetadataSize(30_000) // big payload from pokemon species
            .build();

    @Autowired
    private RedisBouncerClient redisBouncerClient;

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
        redis = TestContainerUtil.buildRedis();
        redisBouncer = TestContainerUtil.buildRedisBouncer(REDIS_BOUNCER_GRPC_PORT);
        redis.start();
        redisBouncer.start();
    }

    @AfterAll
    static void afterAll() {
        redis.stop();
        redisBouncer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        var grpcRedisBouncerAddress = "static://0.0.0.0:".concat(REDIS_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.redis-bouncer.address", () -> grpcRedisBouncerAddress);
    }

    @Test
    void shouldCacheGenerationResponse() {
        var stub = PokeApiGrpc.newBlockingStub(MANAGED_CHANNEL);
        // Check redis cache - should be empty
        var entryRequest = EntryRequest.newBuilder()
                .setKey(RedisKeyPrefix.GENERATION_KEY_PREFIX.getPrefix().concat(Generation.GENERATION_I.getName()))
                .build();
        var cache = redisBouncerClient.valueOps()
                .get(entryRequest, GenerationResponsePrototype.class);
        assertThat(cache).isEmpty();
        // Fetch generation-i
        var response = stub.fetchGeneration(StringValue.newBuilder()
                .setValue(Generation.GENERATION_I.getName())
                .build());
        assertThat(response.getNameList())
                .isNotEmpty()
                .hasSize(151);
        // Check redis cache - should contain entries
        cache = redisBouncerClient.valueOps()
                .get(entryRequest, GenerationResponsePrototype.class);
        assertThat(cache).isNotEmpty();
        assertThat(cache.get().getNameList())
                .isNotEmpty()
                .hasSize(151);
    }

    @Test
    void shouldCachePokemon() {
        var stub = PokeApiGrpc.newBlockingStub(MANAGED_CHANNEL);
        // Check redis cache - should be empty
        var entryRequest = EntryRequest.newBuilder()
                .setKey(RedisKeyPrefix.POKEMON_KEY_PREFIX.getPrefix().concat("bulbasaur"))
                .build();
        var cache = redisBouncerClient.valueOps()
                .get(entryRequest, PokemonResponsePrototype.class);
        assertThat(cache).isEmpty();
        // Fetch pokemon
        var response = stub.fetchPokemon(StringValue.newBuilder()
                .setValue("bulbasaur")
                .build());
        assertThat(response.getName()).isEqualTo("bulbasaur");
        // Check redis cache - should contain entries
        cache = redisBouncerClient.valueOps()
                .get(entryRequest, PokemonResponsePrototype.class);
        assertThat(cache).isNotEmpty();
    }

    @Test
    void shouldCacheSpecies() {
        var stub = PokeApiGrpc.newBlockingStub(MANAGED_CHANNEL);
        // Check redis cache - should be empty
        var entryRequest = EntryRequest.newBuilder()
                .setKey(RedisKeyPrefix.SPECIES_KEY_PREFIX.getPrefix().concat("charizard"))
                .build();
        var cache = redisBouncerClient.valueOps()
                .get(entryRequest, PokemonSpeciesResponsePrototype.class);
        assertThat(cache).isEmpty();
        // Fetch species
        var response = stub.fetchPokemonSpecies(StringValue.newBuilder()
                .setValue("charizard")
                .build());
        assertThat(response.getName()).isEqualTo("charizard");
        // Check redis cache - should contain entries
        cache = redisBouncerClient.valueOps()
                .get(entryRequest, PokemonSpeciesResponsePrototype.class);
        assertThat(cache).isNotEmpty();
    }
}