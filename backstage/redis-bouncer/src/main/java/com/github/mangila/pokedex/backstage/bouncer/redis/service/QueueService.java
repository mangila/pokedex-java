package com.github.mangila.pokedex.backstage.bouncer.redis.service;

import com.github.mangila.pokedex.backstage.model.grpc.HelloReply;
import com.github.mangila.pokedex.backstage.model.grpc.HelloRequest;
import com.github.mangila.pokedex.backstage.model.grpc.SimpleGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class QueueService extends SimpleGrpc.SimpleImplBase {

    private final RedisTemplate<String, Object> redisObjectTemplate;
    private final RedisTemplate<String, String> redisStringTemplate;

    public QueueService(RedisTemplate<String, Object> redisObjectTemplate,
                        RedisTemplate<String, String> redisStringTemplate) {
        this.redisObjectTemplate = redisObjectTemplate;
        this.redisStringTemplate = redisStringTemplate;
    }

    public void add(String queueName, Object value) {
        redisObjectTemplate.opsForSet().add(queueName, value);
    }

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        var k = redisStringTemplate.opsForValue().setIfAbsent("hej", "hej");
        responseObserver.onNext(HelloReply.newBuilder()
                .setMessage(k.toString())
                .build());
        responseObserver.onCompleted();
    }


    public void add(String queueName, String value) {
        redisStringTemplate.opsForSet().add(queueName, value);
    }

    public Object pop(String queueName) {
        return redisObjectTemplate.opsForSet().pop(queueName);
    }

    public String popAsString(String queueName) {
        return redisStringTemplate.opsForSet().pop(queueName);
    }
}
