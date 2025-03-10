package com.github.mangila.pokedex.backstage.shared.bouncer.imageconverter;

import com.github.mangila.pokedex.backstage.model.grpc.model.MediaValue;

public interface ImageConverter {
    MediaValue convertToWebP(MediaValue mediaValue);
}
