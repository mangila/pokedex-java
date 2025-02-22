package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.SetGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.SetRequest;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.util.Strings;
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
    public void add(SetRequest request, StreamObserver<Int64Value> responseObserver) {
        var queueName = request.getQueueName();
        var data = request.getData();
        var response = redisTemplate.opsForSet().add(queueName, data);
        if (Objects.isNull(response)) {
            responseObserver.onError(new RuntimeException("failed to unbox redis set add response"));
        } else {
            responseObserver.onNext(
                    Int64Value.newBuilder()
                            .setValue(response)
                            .build()
            );
            responseObserver.onCompleted();
        }
    }

    @Override
    public void pop(SetRequest request, StreamObserver<StringValue> responseObserver) {
        var queueName = request.getQueueName();
        var data = redisTemplate.opsForSet().pop(queueName);
        if (Objects.isNull(data)) {
            responseObserver.onNext(StringValue.newBuilder()
                    .setValue(Strings.EMPTY)
                    .build());
        } else {
            responseObserver.onNext(StringValue.newBuilder()
                    .setValue(data)
                    .build());
        }
        responseObserver.onCompleted();
    }
}
