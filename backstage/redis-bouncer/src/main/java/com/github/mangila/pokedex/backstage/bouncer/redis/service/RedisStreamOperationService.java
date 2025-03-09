package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.stream.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisConsumerGroup;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

@GrpcService
public class RedisStreamOperationService extends StreamOperationGrpc.StreamOperationImplBase {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamOperationService.class);
    private static final String CONSUMER = "redis-bouncer";
    private static final String CONSUMER_GROUP = RedisConsumerGroup.POKEDEX_BACKSTAGE_GROUP.getGroupName();

    private final StringRedisTemplate template;

    public RedisStreamOperationService(@Qualifier("template") StringRedisTemplate template) {
        this.template = template;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readOne(StreamRecord request, StreamObserver<StreamRecord> responseObserver) {
        var messages = template.opsForStream()
                .read(Consumer.from(CONSUMER_GROUP, CONSUMER),
                        StreamReadOptions.empty()
                                .block(Duration.ofSeconds(5))
                                .count(1),
                        StreamOffset.create(request.getStreamKey(), ReadOffset.lastConsumed()));
        if (CollectionUtils.isEmpty(messages)) {
            responseObserver.onNext(StreamRecord.newBuilder()
                    .setStreamKey(request.getStreamKey())
                    .setRecordId(Strings.EMPTY)
                    .putAllData(Collections.emptyMap())
                    .build());
        } else {
            messages.forEach(message -> {
                var recordId = message.getId().getValue();
                var data = Stream.of(message.getValue())
                        .map(Map::entrySet)
                        .flatMap(Collection::stream)
                        .map(entry -> Map.of((String) entry.getKey(), (String) entry.getValue()))
                        .findFirst()
                        .orElse(Collections.emptyMap());
                responseObserver.onNext(
                        StreamRecord.newBuilder()
                                .setStreamKey(request.getStreamKey())
                                .setRecordId(recordId)
                                .putAllData(data)
                                .build()
                );
            });
        }
        responseObserver.onCompleted();
    }

    @Override
    public void acknowledgeOne(StreamRecord request, StreamObserver<Empty> responseObserver) {
        template.opsForStream().acknowledge(
                request.getStreamKey(),
                CONSUMER_GROUP,
                request.getRecordId()
        );
        responseObserver.onNext(Empty.getDefaultInstance());
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
                log.debug("Stream finished");
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            }
        };
    }
}
