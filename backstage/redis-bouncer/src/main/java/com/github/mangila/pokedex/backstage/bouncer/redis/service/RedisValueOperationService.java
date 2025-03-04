package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.github.mangila.pokedex.backstage.model.grpc.redis.ValueOperationGrpc;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Objects;

@GrpcService
public class RedisValueOperationService extends ValueOperationGrpc.ValueOperationImplBase {

    private static final Logger log = LoggerFactory.getLogger(RedisValueOperationService.class);

    private final StringRedisTemplate template;

    public RedisValueOperationService(@Qualifier("template") StringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public void set(EntryRequest request, StreamObserver<Empty> responseObserver) {
        log.debug("{}", request.toString());
        template.opsForValue()
                .set(request.getKey(), request.getValue());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void get(EntryRequest request, StreamObserver<StringValue> responseObserver) {
        log.debug("{}", request.toString());
        var value = template.opsForValue()
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
}
