package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.SimpleGrpc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class RedisBouncerClientConfig {

    private final RedisBouncerProps redisBouncerProps;

    public RedisBouncerClientConfig(RedisBouncerProps redisBouncerProps) {
        this.redisBouncerProps = redisBouncerProps;
    }

    @Bean
    @ConditionalOnProperty(name = "app.integration.bouncer.redis")
    public SimpleGrpc.SimpleBlockingStub stub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(redisBouncerProps.getHost());
        return SimpleGrpc.newBlockingStub(channel);
    }
}
