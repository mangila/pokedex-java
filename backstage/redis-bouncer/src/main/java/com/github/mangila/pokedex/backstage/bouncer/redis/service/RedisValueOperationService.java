package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.model.ValueRequest;
import com.github.mangila.pokedex.backstage.model.grpc.service.ValueOperationGrpc;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@GrpcService
public class RedisValueOperationService extends ValueOperationGrpc.ValueOperationImplBase {

    private static final Logger log = LoggerFactory.getLogger(RedisValueOperationService.class);

    private static final Base64.Decoder BASE_64_DECODER = Base64.getDecoder();
    private static final Base64.Encoder BASE_64_ENCODER = Base64.getEncoder();

    private final StringRedisTemplate template;

    public RedisValueOperationService(@Qualifier("template") StringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public void set(ValueRequest request, StreamObserver<Empty> responseObserver) {
        log.debug("{}", request.toString());
        template.opsForValue()
                .set(request.getKey(), BASE_64_ENCODER.encodeToString(request.getValue().toByteArray()));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void get(ValueRequest request, StreamObserver<Any> responseObserver) {
        String value = template.opsForValue().get(request.getKey());
        if (Objects.isNull(value)) {
            responseObserver.onNext(Any.getDefaultInstance());
        } else {
            var decodedValue = BASE_64_DECODER.decode(value);
            var parse = parseFrom(decodedValue);
            parse.ifPresentOrElse(responseObserver::onNext, () -> responseObserver.onNext(Any.getDefaultInstance()));
        }
        responseObserver.onCompleted();
    }

    private Optional<Any> parseFrom(byte[] value) {
        try {
            return Optional.ofNullable(Any.parseFrom(value));
        } catch (InvalidProtocolBufferException e) {
            log.error("ERR", e);
            return Optional.empty();
        }
    }
}
