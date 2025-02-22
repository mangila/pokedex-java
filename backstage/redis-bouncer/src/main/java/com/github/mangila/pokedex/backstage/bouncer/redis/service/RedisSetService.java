package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.SetGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.SetRequest;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Objects;

@GrpcService
public class RedisSetService extends SetGrpc.SetImplBase {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisSetService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void add(SetRequest request, StreamObserver<Empty> responseObserver) {
        var queueName = request.getQueueName();
        var data = request.getData();
        redisTemplate.opsForSet().add(queueName, data);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void pop(SetRequest request, StreamObserver<StringValue> responseObserver) {
        var queueName = request.getQueueName();
        var data = redisTemplate.opsForSet().pop(queueName);
        if (Objects.nonNull(data)) {
            responseObserver.onNext(StringValue.newBuilder()
                    .setValue(data)
                    .build());
        }
        responseObserver.onCompleted();
    }
}
