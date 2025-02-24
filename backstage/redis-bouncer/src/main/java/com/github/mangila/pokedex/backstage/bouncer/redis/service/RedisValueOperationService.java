package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.redis.ValueOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.ValueOperationRequest;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Objects;

@GrpcService
public class RedisValueOperationService extends ValueOperationGrpc.ValueOperationImplBase {

    private static final Logger log = LoggerFactory.getLogger(RedisValueOperationService.class);

    private final RedisTemplate<String, String> redisTemplate;

    public RedisValueOperationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void get(ValueOperationRequest request, StreamObserver<StringValue> responseObserver) {
        var value = redisTemplate.opsForValue()
                .get(request.getKey());
        if (Objects.isNull(value)) {
            responseObserver.onNext(StringValue.newBuilder()
                    .setValue(Strings.EMPTY)
                    .build());
        } else {
            responseObserver.onNext(StringValue.newBuilder()
                    .setValue(value)
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void set(ValueOperationRequest request, StreamObserver<Empty> responseObserver) {
        redisTemplate.opsForValue()
                .set(request.getKey(), request.getData());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
