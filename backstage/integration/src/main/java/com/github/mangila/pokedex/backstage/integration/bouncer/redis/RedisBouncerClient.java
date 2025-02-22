package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mangila.pokedex.backstage.model.grpc.SetGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.SetRequest;
import com.github.mangila.pokedex.backstage.model.grpc.ValueGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.ValueRequest;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import org.springframework.stereotype.Service;

@Service
public class RedisBouncerClient {

    private final SetGrpc.SetBlockingStub setBlockingStub;
    private final ValueGrpc.ValueBlockingStub valueBlockingStub;
    private final ObjectMapper objectMapper;

    public RedisBouncerClient(SetGrpc.SetBlockingStub setBlockingStub,
                              ValueGrpc.ValueBlockingStub valueBlockingStub,
                              ObjectMapper objectMapper) {
        this.setBlockingStub = setBlockingStub;
        this.valueBlockingStub = valueBlockingStub;
        this.objectMapper = objectMapper;
    }

    public Empty set(String key, String value) {
        return valueBlockingStub.set(
                ValueRequest.newBuilder()
                        .setKey(key)
                        .setData(value)
                        .build()
        );
    }

    public <T> T get(String key, Class<T> clazz) {
        var request = ValueRequest.newBuilder()
                .setKey(key)
                .build();
        var response = valueBlockingStub.get(request);
        var jsonString = response.getValue();
        if (jsonString.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Int64Value add(String queueName, String data) {
        var request = SetRequest.newBuilder()
                .setQueueName(queueName)
                .setData(data)
                .build();
        return setBlockingStub.add(request);
    }

    public <T> T pop(String queueName, Class<T> clazz) {
        var request = SetRequest.newBuilder()
                .setQueueName(queueName)
                .build();
        var response = setBlockingStub.pop(request);
        var jsonString = response.getValue();
        if (jsonString.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
