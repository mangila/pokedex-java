package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.mongodb.GridFsGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.mongodb.MongoDbGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class MongoBouncerClientConfig {

    @Bean
    public MongoDbGrpc.MongoDbBlockingStub mongoDbBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "mongodb-bouncer",
                ChannelBuilderOptions.defaults());
        return MongoDbGrpc.newBlockingStub(channel);
    }

    @Bean
    public GridFsGrpc.GridFsBlockingStub gridFsBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "mongodb-bouncer",
                ChannelBuilderOptions.defaults());
        return GridFsGrpc.newBlockingStub(channel);
    }
}
