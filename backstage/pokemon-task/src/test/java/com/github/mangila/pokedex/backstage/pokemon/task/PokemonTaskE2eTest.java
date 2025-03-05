package com.github.mangila.pokedex.backstage.pokemon.task;

import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Disabled(value = "Run only where a Docker env is available - redis-bouncer and mongodb-bouncer server needs to be in a Container")
class PokemonTaskE2eTest {

    private static final String GRPC_PORT = "9999";
    @Autowired
    private Task pokemonTask;
    @Autowired
    private RedisBouncerClient redisBouncerClient;

    private static final DockerImageName REDIS_CONTAINER_NAME = DockerImageName.parse("redis:7.4.2-alpine");
    private static final DockerImageName MONGODB_IMAGE_NAME = DockerImageName.parse("mongo");
    private static final DockerImageName REDIS_BOUNCER_CONTAINER_NAME = DockerImageName.parse("mangila/pokedex-redis-bouncer");
    private static final DockerImageName MONGO_DB_BOUNCER_CONTAINER_NAME = DockerImageName.parse("mangila/pokedex-mongodb-bouncer");

    @SuppressWarnings("rawtypes")
    public static GenericContainer redis;
    private static MongoDBContainer mongoDb;
    @SuppressWarnings("rawtypes")
    public static GenericContainer redisBouncer;
    @SuppressWarnings("rawtypes")
    public static GenericContainer mongoDbBouncer;

    @SuppressWarnings({"rawtypes", "unchecked"})
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
                        .withStartupTimeout(Duration.ofSeconds(1)));
        redisBouncer.setPortBindings(List.of(
                GRPC_PORT.concat(":").concat(GRPC_PORT) //host:container port
        ));
        redisBouncer.start();
        mongoDb = new MongoDBContainer(MONGODB_IMAGE_NAME)
                .withNetworkAliases("mongo")
                .withNetwork(Network.SHARED);
        mongoDb.start();
        mongoDbBouncer = new GenericContainer(MONGO_DB_BOUNCER_CONTAINER_NAME)
                .withNetwork(Network.SHARED)
                .waitingFor(new LogMessageWaitStrategy()
                        .withRegEx(".*gRPC Server started.*")
                        .withTimes(1)
                        .withStartupTimeout(Duration.ofSeconds(1)));
        mongoDbBouncer.setPortBindings(List.of(
                GRPC_PORT.concat(":").concat(GRPC_PORT) //host:container port
        ));

    }

    @AfterAll
    static void afterAll() {
        redis.stop();
        redis.close();
        redisBouncer.stop();
        redisBouncer.close();
        mongoDb.stop();
        mongoDb.close();
        mongoDbBouncer.stop();
        mongoDbBouncer.close();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        var grpcAddress = "static://0.0.0.0:".concat(GRPC_PORT);
        registry.add("spring.grpc.client.channels.redis-bouncer.address", () -> grpcAddress);
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