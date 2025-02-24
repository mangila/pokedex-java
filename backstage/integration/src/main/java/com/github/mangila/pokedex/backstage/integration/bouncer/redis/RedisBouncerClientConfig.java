package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.redis.SetOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.ValueOperationGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class RedisBouncerClientConfig {

    @Bean
    public SetOperationGrpc.SetOperationBlockingStub setOperationBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "redis-bouncer");
        return SetOperationGrpc.newBlockingStub(channel);
    }

    @Bean
    public SetOperationGrpc.SetOperationStub setOperationFutureStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "redis-bouncer");
        return SetOperationGrpc.newStub(channel);
    }

    @Bean
    public ValueOperationGrpc.ValueOperationBlockingStub valueOperationBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "redis-bouncer");
        return ValueOperationGrpc.newBlockingStub(channel);
    }
}
