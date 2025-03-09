package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.stream.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisConsumerGroup;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {
        "spring.grpc.server.port=32768"
})
@Testcontainers
@Disabled(value = "Run only where a Docker env is available")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisStreamOperationServiceTest extends RedisTestContainer {

    private static final ManagedChannel MANAGED_CHANNEL = ManagedChannelBuilder.forAddress("0.0.0.0", 32768)
            .usePlaintext()
            .build();

    @Autowired
    private StringRedisTemplate template;

    @Test
    @Order(1)
    void addWithClientStream() throws InterruptedException {
        var stub = StreamOperationGrpc.newStub(MANAGED_CHANNEL);
        var streamKey = RedisStreamKey.POKEMON_NAME_EVENT.getKey();
        var observer = stub.addWithClientStream(new StreamObserver<>() {
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
                .setStreamKey(streamKey)
                .putData("name", "bulbasaur")
                .build());
        observer.onNext(StreamRecord.newBuilder()
                .setStreamKey(streamKey)
                .putData("name", "charizard")
                .build());
        observer.onCompleted();
        await().atMost(5, SECONDS)
                .until(() -> {
                    var message = template.opsForStream().info(streamKey);
                    return message.streamLength() == 2;
                });
    }

    @Test
    @Order(2)
    void readFirst() {
        var stub = StreamOperationGrpc.newBlockingStub(MANAGED_CHANNEL);
        var readOne = stub.readOne(StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .build());
        assertThat(readOne).isNotNull();
        assertThat(readOne.getRecordId())
                .isNotNull()
                .isNotBlank();
        assertThat(readOne.getDataMap())
                .hasSize(1)
                .containsEntry("name", "bulbasaur");
        await().atMost(5, SECONDS)
                .until(() -> {
                    var pending = template.opsForStream()
                            .pending(readOne.getStreamKey(), RedisConsumerGroup.POKEDEX_BACKSTAGE_GROUP.getGroupName())
                            .getTotalPendingMessages();
                    return pending == 1;
                });
        stub.acknowledgeOne(StreamRecord.newBuilder()
                .setStreamKey(readOne.getStreamKey())
                .setRecordId(readOne.getRecordId())
                .build());
        await().atMost(5, SECONDS)
                .until(() -> {
                    var pending = template.opsForStream()
                            .pending(readOne.getStreamKey(), RedisConsumerGroup.POKEDEX_BACKSTAGE_GROUP.getGroupName())
                            .getTotalPendingMessages();
                    return pending == 0;
                });
    }

    @Test
    @Order(3)
    void readSecond() {
        var stub = StreamOperationGrpc.newBlockingStub(MANAGED_CHANNEL);
        var readOne = stub.readOne(StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .build());
        assertThat(readOne).isNotNull();
        assertThat(readOne.getRecordId())
                .isNotNull()
                .isNotBlank();
        assertThat(readOne.getDataMap())
                .hasSize(1)
                .containsEntry("name", "charizard");
        await().atMost(5, SECONDS)
                .until(() -> {
                    var pending = template.opsForStream()
                            .pending(readOne.getStreamKey(), RedisConsumerGroup.POKEDEX_BACKSTAGE_GROUP.getGroupName())
                            .getTotalPendingMessages();
                    return pending == 1;
                });
        stub.acknowledgeOne(StreamRecord.newBuilder()
                .setStreamKey(readOne.getStreamKey())
                .setRecordId(readOne.getRecordId())
                .build());
        await().atMost(5, SECONDS)
                .until(() -> {
                    var pending = template.opsForStream()
                            .pending(readOne.getStreamKey(), RedisConsumerGroup.POKEDEX_BACKSTAGE_GROUP.getGroupName())
                            .getTotalPendingMessages();
                    return pending == 0;
                });
    }

    @Test
    @Order(4)
    void readEmpty() {
        var stub = StreamOperationGrpc.newBlockingStub(MANAGED_CHANNEL);
        var readOne = stub.readOne(StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .build());
        assertThat(readOne).isNotNull();
        assertThat(readOne.getRecordId())
                .isNotNull()
                .isEmpty();
        assertThat(readOne.getDataMap()).isEmpty();
        await().atMost(5, SECONDS)
                .until(() -> {
                    var pending = template.opsForStream()
                            .pending(readOne.getStreamKey(), RedisConsumerGroup.POKEDEX_BACKSTAGE_GROUP.getGroupName())
                            .getTotalPendingMessages();
                    return pending == 0;
                });
    }
}