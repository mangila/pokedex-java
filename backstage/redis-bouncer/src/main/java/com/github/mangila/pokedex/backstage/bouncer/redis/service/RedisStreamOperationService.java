package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.RedisConsumerGroup;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamRecord;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@GrpcService
public class RedisStreamOperationService extends StreamOperationGrpc.StreamOperationImplBase {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamOperationService.class);

    private static final String CONSUMER = "redis-bouncer";
    private final StringRedisTemplate template;

    public RedisStreamOperationService(@Qualifier("template") StringRedisTemplate template) {
        this.template = template;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readOne(StreamRecord request, StreamObserver<StreamRecord> responseObserver) {
        var messages = template
                .opsForStream()
                .read(Consumer.from(
                                RedisConsumerGroup.POKEDEX_BACKSTAGE_GROUP.getGroupName(), CONSUMER),
                        StreamReadOptions.empty()
                                .block(Duration.ofSeconds(5))
                                .count(1),
                        StreamOffset.create(request.getStreamKey(), ReadOffset.lastConsumed()));
        Map<String, String> value = messages.stream()
                .map(Record::getValue)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .map(entry -> Map.of((String) entry.getKey(), (String) entry.getValue()))
                .findFirst()
                .orElse(Collections.emptyMap());
        responseObserver.onNext(StreamRecord.newBuilder()
                .setStreamKey(request.getStreamKey())
                .putAllData(value)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<StreamRecord> addWithClientStream(StreamObserver<Empty> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(StreamRecord streamRecord) {
                log.debug("{}", streamRecord.toString());
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
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            }
        };
    }
}
