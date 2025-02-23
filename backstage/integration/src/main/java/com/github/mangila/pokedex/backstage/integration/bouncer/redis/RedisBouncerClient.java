package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mangila.pokedex.backstage.model.grpc.SetOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.SetOperationRequest;
import com.github.mangila.pokedex.backstage.model.grpc.ValueOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.ValueOperationRequest;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RedisBouncerClient {

    private final SetOperationGrpc.SetOperationBlockingStub setBlockingStub;
    private final ValueOperationGrpc.ValueOperationBlockingStub valueBlockingStub;
    private final ObjectMapper objectMapper;

    public RedisBouncerClient(SetOperationGrpc.SetOperationBlockingStub setBlockingStub,
                              ValueOperationGrpc.ValueOperationBlockingStub valueBlockingStub,
                              ObjectMapper objectMapper) {
        this.setBlockingStub = setBlockingStub;
        this.valueBlockingStub = valueBlockingStub;
        this.objectMapper = objectMapper;
    }


    public Empty set(String key, String value) {
        return valueBlockingStub.set(
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
        var response = valueBlockingStub.get(request);
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

    public Int64Value add(String queueName, String data) {
        var request = SetOperationRequest.newBuilder()
                .setQueueName(queueName)
                .setData(data)
                .build();
        return setBlockingStub.add(request);
    }

    public <T> Optional<T> pop(String queueName, Class<T> clazz) {
        var request = SetOperationRequest.newBuilder()
                .setQueueName(queueName)
                .build();
        var response = setBlockingStub.pop(request);
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
