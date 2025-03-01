package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.RedisStreamKey;
import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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

@SpringBootTest(classes = {
        RedisBouncerClientApplicationTest.class
})
@Testcontainers
@Disabled(value = "Run only where a Docker env is available - redis-bouncer server needs to be in a Container")
class RedisBouncerClientTest {

    private static final String GRPC_PORT = "9999";

    @Autowired
    private RedisBouncerClient redisBouncerClient;

    private static final DockerImageName REDIS_CONTAINER_NAME = DockerImageName.parse("redis:7.4.2-alpine");
    private static final DockerImageName REDIS_BOUNCER_CONTAINER_NAME = DockerImageName.parse("mangila/pokedex-redis-bouncer");

    @SuppressWarnings("rawtypes")
    public static GenericContainer redis;
    @SuppressWarnings("rawtypes")
    public static GenericContainer redisBouncer;

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
    void testGetAndSet() {
        redisBouncerClient.valueOps()
                .set(EntryRequest.newBuilder()
                        .setKey("key1")
                        .setValue("value1")
                        .build());
        var value = redisBouncerClient.valueOps().get(EntryRequest
                .newBuilder()
                .setKey("key1")
                .build());
        assertThat(value).isNotEmpty();
        assertThat(value).get().isEqualTo("value1");
    }

    @Test
    void testStreamLog() {
        var observer = redisBouncerClient.streamOps()
                .addWithClientStream(new StreamObserver<>() {
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
        observer.onNext(StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .putData("name", "charizard")
                .build());
        observer.onCompleted();

        var readOne = redisBouncerClient.streamOps().readOne(StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .build());
        assertThat(readOne.getDataMap())
                .hasSize(1)
                .containsEntry("name", "bulbasaur");
        readOne = redisBouncerClient.streamOps().readOne(StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .build());
        assertThat(readOne.getDataMap())
                .hasSize(1)
                .containsEntry("name", "charizard");
        readOne = redisBouncerClient.streamOps().readOne(StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .build());
        assertThat(readOne.getDataMap()).isEmpty();
    }
}