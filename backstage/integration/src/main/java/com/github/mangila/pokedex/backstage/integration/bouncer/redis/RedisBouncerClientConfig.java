package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.SetOperationGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.ValueOperationGrpc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class RedisBouncerClientConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.grpc.client.channels.redis-bouncer", matchIfMissing = true)
    public SetOperationGrpc.SetOperationBlockingStub setBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "redis-bouncer");
        return SetOperationGrpc.newBlockingStub(channel);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.grpc.client.channels.redis-bouncer", matchIfMissing = true)
    public ValueOperationGrpc.ValueOperationBlockingStub valueBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "redis-bouncer");
        return ValueOperationGrpc.newBlockingStub(channel);
    }
}
