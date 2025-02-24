package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.redis.SetOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.SetOperationRequest;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Objects;

@GrpcService
public class RedisSetOperationService extends SetOperationGrpc.SetOperationImplBase {

    private static final Logger log = LoggerFactory.getLogger(RedisSetOperationService.class);

    private final RedisTemplate<String, String> redisTemplate;

    public RedisSetOperationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public StreamObserver<SetOperationRequest> addBiDirectionalStream(StreamObserver<Int64Value> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(SetOperationRequest request) {
                var queueName = request.getQueueName();
                var data = request.getData();
                var redisResponse = redisTemplate.opsForSet().add(queueName, data);
                if (Objects.isNull(redisResponse)) {
                    responseObserver.onError(new RuntimeException("failed to unbox redis set add response"));
                } else {
                    responseObserver.onNext(
                            Int64Value.newBuilder()
                                    .setValue(redisResponse)
                                    .build()
                    );
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void add(SetOperationRequest request, StreamObserver<Int64Value> responseObserver) {
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
    public void pop(SetOperationRequest request, StreamObserver<StringValue> responseObserver) {
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
