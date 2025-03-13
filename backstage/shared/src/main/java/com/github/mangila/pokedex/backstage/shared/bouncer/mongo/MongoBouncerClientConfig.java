package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.service.GridFsGrpc;
import com.github.mangila.pokedex.backstage.model.grpc.service.MongoDbGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class MongoBouncerClientConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoBouncerClientConfig.class);

    @Bean
    public MongoDbGrpc.MongoDbBlockingStub mongoDbBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "mongodb-bouncer",
                ChannelBuilderOptions.defaults());
        log.info("mongoDbBlockingStub - {}", channel.authority());
        return MongoDbGrpc.newBlockingStub(channel);
    }

    @Bean
    public GridFsGrpc.GridFsBlockingStub gridFsBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "mongodb-bouncer",
                ChannelBuilderOptions.defaults());
        log.info("gridFsBlockingStub - {}", channel.authority());
        return GridFsGrpc.newBlockingStub(channel);
    }
}
