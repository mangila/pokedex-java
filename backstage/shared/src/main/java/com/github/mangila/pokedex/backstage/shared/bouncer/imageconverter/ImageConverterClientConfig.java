package com.github.mangila.pokedex.backstage.shared.bouncer.imageconverter;

import com.github.mangila.pokedex.backstage.model.grpc.service.ImageConverterGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class ImageConverterClientConfig {

    private static final Logger log = LoggerFactory.getLogger(ImageConverterClientConfig.class);

    @Bean
    public ImageConverterGrpc.ImageConverterBlockingStub imageConverterBlockingStub(GrpcChannelFactory channels) {
        var channel = channels.createChannel(
                "image-converter",
                ChannelBuilderOptions.defaults());
        log.info("imageConverterBlockingStub - {}", channel.authority());
        return ImageConverterGrpc.newBlockingStub(channel);
    }
}
