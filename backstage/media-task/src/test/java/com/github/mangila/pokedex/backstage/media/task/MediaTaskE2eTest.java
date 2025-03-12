package com.github.mangila.pokedex.backstage.media.task;

import com.github.mangila.pokedex.backstage.model.grpc.model.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Disabled(value = "Run only where a Docker env is available")
class MediaTaskE2eTest {

    private static final String REDIS_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();
    private static final String MONGO_DB_BOUNCER_GRPC_PORT = generateRandomEphemeralPort();
    private static final String IMAGE_CONVERTER_GRPC_PORT = generateRandomEphemeralPort();

    @Autowired
    private Task mediaTask;
    @Autowired
    private RedisBouncerClient redisBouncerClient;

    private static GenericContainer<?> imageConverter;
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
        imageConverter = TestContainerUtil.buildImageConverter(IMAGE_CONVERTER_GRPC_PORT);
        redis = TestContainerUtil.buildRedis();
        redisBouncer = TestContainerUtil.buildRedisBouncer(REDIS_BOUNCER_GRPC_PORT);
        mongoDb = TestContainerUtil.buildMongoDb();
        mongoDbBouncer = TestContainerUtil.buildMongoDbBouncer(MONGO_DB_BOUNCER_GRPC_PORT);
        imageConverter.start();
        redis.start();
        redisBouncer.start();
        mongoDb.start();
        mongoDbBouncer.start();
    }

    @AfterAll
    static void afterAll() {
        imageConverter.stop();
        redis.stop();
        redisBouncer.stop();
        mongoDb.stop();
        mongoDbBouncer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        var imageConverterAddress = "static://0.0.0.0:".concat(IMAGE_CONVERTER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.image-converter.address", () -> imageConverterAddress);
        var redisBouncerAddress = "static://0.0.0.0:".concat(REDIS_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.redis-bouncer.address", () -> redisBouncerAddress);
        var mongoDbBouncerAddress = "static://0.0.0.0:".concat(MONGO_DB_BOUNCER_GRPC_PORT);
        registry.add("spring.grpc.client.channels.mongodb-bouncer.address", () -> mongoDbBouncerAddress);
    }

    @Test
    void shouldNotThrowAnyException() {
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
                .setStreamKey(RedisStreamKey.POKEMON_MEDIA_EVENT.getKey())
                .putData("url", "https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/1.ogg")
                .putData("description", "latest")
                .putData("species_id", "1")
                .putData("pokemon_id", "1")
                .putData("pokemon_name", "bulbasaur")
                .build());
        observer.onNext(StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_MEDIA_EVENT.getKey())
                .putData("url", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png")
                .putData("description", "front-default")
                .putData("species_id", "1")
                .putData("pokemon_id", "1")
                .putData("pokemon_name", "bulbasaur")
                .build());
        observer.onCompleted();
        assertThatCode(() -> mediaTask.run(new String[0])).doesNotThrowAnyException();
        assertThatCode(() -> mediaTask.run(new String[0])).doesNotThrowAnyException();
    }
}