package com.github.mangila.pokedex.backstage.pokemon.task;

import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Disabled(value = "Run only where a Docker env is available - redis-bouncer and mongodb-bouncer server needs to be in a Container")
class PokemonTaskE2eTest {

    private static final String POKE_API_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();
    private static final String REDIS_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();
    private static final String MONGO_DB_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();
    @Autowired
    private Task pokemonTask;
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
        TestContainerUtil.buildPokeApiBouncer(POKE_API_BOUNCER_GRPC_PORT)
                .start();
        TestContainerUtil.buildRedis()
                .start();
        TestContainerUtil.buildRedisBouncer(REDIS_BOUNCER_GRPC_PORT)
                .start();
        TestContainerUtil.buildMongoDb()
                .start();
        TestContainerUtil.buildMongoDbBouncer(MONGO_DB_BOUNCER_GRPC_PORT)
                .start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        var pokeApiBouncerAddress = "static://0.0.0.0:".concat(POKE_API_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.pokeapi-bouncer.address", () -> pokeApiBouncerAddress);
        var redisBouncerAddress = "static://0.0.0.0:".concat(REDIS_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.redis-bouncer.address", () -> redisBouncerAddress);
        var mongoDbBouncerAddress = "static://0.0.0.0:".concat(MONGO_DB_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.mongodb-bouncer.address", () -> mongoDbBouncerAddress);
    }

    @Test
    void testRun() {
        var observer = redisBouncerClient.streamOps().addWithClientStream(new StreamObserver<>() {
            @Override
            public void onNext(Empty empty) {
                // do nothing
            }

            @Override
            public void onError(Throwable throwable) {
                Assertions.fail(throwable);
            }

            @Override
            public void onCompleted() {
                // do nothing
            }
        });
        observer.onNext(StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .putData("name", "bulbasaur")
                .build());
        observer.onCompleted();
        assertThatCode(() -> pokemonTask.run(new String[0])).doesNotThrowAnyException();
    }
}