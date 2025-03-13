package com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi;

import com.github.mangila.pokedex.backstage.model.grpc.service.PokeApiGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class PokeApiBouncerClientConfig {

    private static final Logger log = LoggerFactory.getLogger(PokeApiBouncerClientConfig.class);

    @Bean
    public PokeApiGrpc.PokeApiBlockingStub blockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "pokeapi-bouncer",
                ChannelBuilderOptions.defaults());
        log.info("blockingStub - {}", channel.authority());
        return PokeApiGrpc.newBlockingStub(channel);
    }

}
