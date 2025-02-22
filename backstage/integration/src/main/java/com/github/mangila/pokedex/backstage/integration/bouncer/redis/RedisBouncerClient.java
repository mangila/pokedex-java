package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mangila.pokedex.backstage.model.grpc.SetGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.SetRequest;
import org.springframework.stereotype.Service;

@Service
public class RedisBouncerClient {

    private final SetGrpc.SetBlockingStub grpcClient;
    private final ObjectMapper objectMapper;

    public RedisBouncerClient(SetGrpc.SetBlockingStub grpcClient,
                              ObjectMapper objectMapper) {
        this.grpcClient = grpcClient;
        this.objectMapper = objectMapper;
    }

    public void add(String queueName, String data) {
        var request = SetRequest.newBuilder()
                .setQueueName(queueName)
                .setData(data)
                .build();
        grpcClient.add(request);
    }

    public <T> T pop(String queueName, Class<T> clazz) {
        var request = SetRequest.newBuilder()
                .setQueueName(queueName)
                .build();
        var response = grpcClient.pop(request);
        var jsonString = response.getValue();
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
