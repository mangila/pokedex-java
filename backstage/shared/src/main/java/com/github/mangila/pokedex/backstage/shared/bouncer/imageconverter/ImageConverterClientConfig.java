package com.github.mangila.pokedex.backstage.shared.bouncer.imageconverter;

import com.github.mangila.pokedex.backstage.model.grpc.service.ImageConverterGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class ImageConverterClientConfig {

    @Bean
    public ImageConverterGrpc.ImageConverterBlockingStub imageConverterBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "imageconverter-bouncer",
                ChannelBuilderOptions.defaults());
        return ImageConverterGrpc.newBlockingStub(channel);
    }
}
