package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.MongoDbOperationGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class MongoBouncerClientConfig {

    @Bean
    public MongoDbOperationGrpc.MongoDbOperationBlockingStub mongoDbOperationBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "mongodb-bouncer",
                ChannelBuilderOptions.defaults());
        return MongoDbOperationGrpc.newBlockingStub(channel);
    }
}
