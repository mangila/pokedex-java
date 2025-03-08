package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.model.grpc.redis.ValueOperationGrpc;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.grpc.server.port=32768"
})
@Testcontainers
@Disabled(value = "Run only where a Docker env is available")
public class RedisValueOperationServiceTest extends RedisTestContainer {

    private static final ManagedChannel MANAGED_CHANNEL = ManagedChannelBuilder.forAddress("0.0.0.0", 32768)
            .usePlaintext()
            .build();

    @Test
    void set() {
        var stub = ValueOperationGrpc.newBlockingStub(MANAGED_CHANNEL);
        stub.set(EntryRequest.newBuilder()
                .setKey("key1")
                .setValue(Any.newBuilder()
                        .setValue(ByteString.copyFromUtf8("value1"))
                        .build())
                .build());
        var value = stub.get(EntryRequest.newBuilder()
                .setKey("key1")
                .build());
        assertThat(value.getValue().toStringUtf8())
                .isEqualTo("value1");
    }

    @Test
    void get() {
        var stub = ValueOperationGrpc.newBlockingStub(MANAGED_CHANNEL);
        var value = stub.get(EntryRequest.newBuilder()
                .setKey("keyNotFound")
                .build());
        assertThat(value.getValue()).isEqualTo(ByteString.EMPTY);
    }
}