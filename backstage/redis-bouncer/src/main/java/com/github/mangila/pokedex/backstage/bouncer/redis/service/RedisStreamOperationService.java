package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class RedisStreamOperationService extends StreamOperationGrpc.StreamOperationImplBase {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamOperationService.class);

    private final StringRedisTemplate template;

    public RedisStreamOperationService(@Qualifier("template") StringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public StreamObserver<StreamRecord> addWithClientStream(StreamObserver<Empty> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(StreamRecord streamRecord) {
                log.debug("Server STREAM ADD received: {}", streamRecord.toString());
                var record = StreamRecords.newRecord()
                        .ofStrings(streamRecord.getDataMap())
                        .withStreamKey(streamRecord.getStreamKey());
                template.opsForStream()
                        .add(record);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("ERR", throwable);
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                log.debug("Client finished stream");
                responseObserver.onCompleted();
            }
        };
    }
}
