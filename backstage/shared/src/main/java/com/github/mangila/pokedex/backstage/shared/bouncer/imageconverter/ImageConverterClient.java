package com.github.mangila.pokedex.backstage.shared.bouncer.imageconverter;

import com.github.mangila.pokedex.backstage.model.grpc.model.MediaValue;
import com.github.mangila.pokedex.backstage.model.grpc.service.ImageConverterGrpc;
import org.springframework.stereotype.Service;

@Service
public class ImageConverterClient implements ImageConverter {

    private final ImageConverterGrpc.ImageConverterBlockingStub blockingStub;

    public ImageConverterClient(ImageConverterGrpc.ImageConverterBlockingStub imageConverterBlockingStub) {
        this.blockingStub = imageConverterBlockingStub;
    }

    @Override
    public MediaValue convertToWebP(MediaValue mediaValue) {
        return blockingStub.convertToWebP(mediaValue);
    }
}
