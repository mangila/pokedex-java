package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.redis.StreamOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.redis.ValueOperationGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class RedisBouncerClientConfig {

    @Bean
    public StreamOperationGrpc.StreamOperationStub streamOperationStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "redis-bouncer",
                ChannelBuilderOptions.defaults());
        return StreamOperationGrpc.newStub(channel);
    }

    @Bean
    public StreamOperationGrpc.StreamOperationBlockingStub streamOperationBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "redis-bouncer",
                ChannelBuilderOptions.defaults());
        return StreamOperationGrpc.newBlockingStub(channel);
    }

    @Bean
    public ValueOperationGrpc.ValueOperationBlockingStub valueOperationBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "redis-bouncer",
                ChannelBuilderOptions.defaults());
        return ValueOperationGrpc.newBlockingStub(channel);
    }
}
