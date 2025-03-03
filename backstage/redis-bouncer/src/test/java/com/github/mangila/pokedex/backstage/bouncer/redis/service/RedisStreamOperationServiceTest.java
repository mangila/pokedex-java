package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Disabled(value = "Run only where a Docker env is available")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisStreamOperationServiceTest extends RedisTestContainer {

    @Test
    @Order(1)
    void addWithClientStream() {
        var channel = ManagedChannelBuilder.forAddress("0.0.0.0", 9000)
                .usePlaintext()
                .build();
        var stub = StreamOperationGrpc.newStub(channel);
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
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .putData("name", "bulbasaur")
                .build());
        observer.onNext(StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .putData("name", "charizard")
                .build());
        observer.onCompleted();
    }

    @Test
    @Order(2)
    void readFirst() {
        var channel = ManagedChannelBuilder.forAddress("0.0.0.0", 9000)
                .usePlaintext()
                .build();
        var stub = StreamOperationGrpc.newBlockingStub(channel);
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
        stub.acknowledgeOne(StreamRecord.newBuilder()
                .setStreamKey(readOne.getStreamKey())
                .setRecordId(readOne.getRecordId())
                .build());
    }

    @Test
    @Order(3)
    void readSecond() {
        var channel = ManagedChannelBuilder.forAddress("0.0.0.0", 9000)
                .usePlaintext()
                .build();
        var stub = StreamOperationGrpc.newBlockingStub(channel);
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
        stub.acknowledgeOne(StreamRecord.newBuilder()
                .setStreamKey(readOne.getStreamKey())
                .setRecordId(readOne.getRecordId())
                .build());
    }

    @Test
    @Order(4)
    void readEmpty() {
        var channel = ManagedChannelBuilder.forAddress("0.0.0.0", 9000)
                .usePlaintext()
                .build();
        var stub = StreamOperationGrpc.newBlockingStub(channel);
        var readOne = stub.readOne(StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_NAME_EVENT.getKey())
                .build());
        assertThat(readOne).isNotNull();
        assertThat(readOne.getRecordId())
                .isNotNull()
                .isEmpty();
        assertThat(readOne.getDataMap()).isEmpty();
    }
}