package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.model.ValueRequest;
import com.github.mangila.pokedex.backstage.model.grpc.service.ValueOperationGrpc;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
class RedisValueOperationServiceTest extends RedisTestContainer {

    private static final ManagedChannel MANAGED_CHANNEL = ManagedChannelBuilder.forAddress("0.0.0.0", 32768)
            .usePlaintext()
            .build();

    @Test
    void set() throws InvalidProtocolBufferException {
        var stub = ValueOperationGrpc.newBlockingStub(MANAGED_CHANNEL);
        stub.set(ValueRequest.newBuilder()
                .setKey("key1")
                .setValue(Any.pack(StringValue.of("value1")))
                .build());
        var value = stub.get(ValueRequest.newBuilder()
                .setKey("key1")
                .build());
        assertThat(value.unpack(StringValue.class).getValue())
                .isEqualTo("value1");
    }

    @Test
    void get() {
        var stub = ValueOperationGrpc.newBlockingStub(MANAGED_CHANNEL);
        var value = stub.get(ValueRequest.newBuilder()
                .setKey("keyNotFound")
                .build());
        assertThat(value.getValue()).isEqualTo(ByteString.EMPTY);
    }
}