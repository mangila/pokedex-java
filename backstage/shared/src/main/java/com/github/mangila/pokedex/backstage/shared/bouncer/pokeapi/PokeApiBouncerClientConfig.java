package com.github.mangila.pokedex.backstage.shared.bouncer.pokeapi;

import com.github.mangila.pokedex.backstage.model.grpc.service.PokeApiGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class PokeApiBouncerClientConfig {

    @Bean
    public PokeApiGrpc.PokeApiBlockingStub blockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "pokeapi-bouncer",
                ChannelBuilderOptions.defaults());
        return PokeApiGrpc.newBlockingStub(channel);
    }

}
