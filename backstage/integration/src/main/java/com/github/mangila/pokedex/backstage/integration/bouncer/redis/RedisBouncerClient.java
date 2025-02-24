package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mangila.pokedex.backstage.model.grpc.redis.SetOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.SetOperationRequest;
import com.github.mangila.pokedex.backstage.model.grpc.redis.ValueOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.ValueOperationRequest;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RedisBouncerClient {

    private static final Logger log = LoggerFactory.getLogger(RedisBouncerClient.class);

    private final SetOperationGrpc.SetOperationBlockingStub setOperationBlockingStub;
    private final SetOperationGrpc.SetOperationStub setOperationStub;
    private final ValueOperationGrpc.ValueOperationBlockingStub valueOperationBlockingStub;
    private final ObjectMapper objectMapper;

    public RedisBouncerClient(SetOperationGrpc.SetOperationBlockingStub setOperationBlockingStub,
                              SetOperationGrpc.SetOperationStub setOperationStub,
                              ValueOperationGrpc.ValueOperationBlockingStub valueOperationBlockingStub,
                              ObjectMapper objectMapper) {
        this.setOperationBlockingStub = setOperationBlockingStub;
        this.setOperationStub = setOperationStub;
        this.valueOperationBlockingStub = valueOperationBlockingStub;
        this.objectMapper = objectMapper;
    }

    public Empty set(String key, String value) {
        return valueOperationBlockingStub.set(
                ValueOperationRequest.newBuilder()
                        .setKey(key)
                        .setData(value)
                        .build()
        );
    }

    public <T> Optional<T> get(String key, Class<T> clazz) {
        var request = ValueOperationRequest.newBuilder()
                .setKey(key)
                .build();
        var response = valueOperationBlockingStub.get(request);
        var jsonString = response.getValue();
        if (jsonString.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(objectMapper.readValue(jsonString, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public StreamObserver<SetOperationRequest> addBiDirectionalStream() {
        return setOperationStub.addBiDirectionalStream(new StreamObserver<>() {
            @Override
            public void onNext(Int64Value int64Value) {
                log.debug(String.valueOf(int64Value.getValue()));
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("ERR", throwable);
            }

            @Override
            public void onCompleted() {
                log.debug("STREAM COMPLETED");
            }
        });
    }

    public Int64Value add(String queueName, String data) {
        var request = SetOperationRequest.newBuilder()
                .setQueueName(queueName)
                .setData(data)
                .build();
        return setOperationBlockingStub.add(request);
    }

    public <T> Optional<T> pop(String queueName, Class<T> clazz) {
        var request = SetOperationRequest.newBuilder()
                .setQueueName(queueName)
                .build();
        var response = setOperationBlockingStub.pop(request);
        var jsonString = response.getValue();
        if (jsonString.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(objectMapper.readValue(jsonString, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
