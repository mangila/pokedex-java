package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.SetGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.ValueGrpc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class RedisBouncerClientConfig {

    private final RedisBouncerProps redisBouncerProps;

    public RedisBouncerClientConfig(RedisBouncerProps redisBouncerProps) {
        this.redisBouncerProps = redisBouncerProps;
    }

    @Bean
    @ConditionalOnProperty(name = "app.integration.bouncer.redis", matchIfMissing = true)
    public SetGrpc.SetBlockingStub setBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                redisBouncerProps.getHost(),
                ChannelBuilderOptions.defaults());
        return SetGrpc.newBlockingStub(channel);
    }

    @Bean
    @ConditionalOnProperty(name = "app.integration.bouncer.redis", matchIfMissing = true)
    public ValueGrpc.ValueBlockingStub valueBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                redisBouncerProps.getHost(),
                ChannelBuilderOptions.defaults());
        return ValueGrpc.newBlockingStub(channel);
    }
}
