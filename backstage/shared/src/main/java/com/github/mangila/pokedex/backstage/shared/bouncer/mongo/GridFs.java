package com.github.mangila.pokedex.backstage.shared.bouncer.mongo;

import com.github.mangila.pokedex.backstage.model.grpc.model.MediaValue;
import com.google.protobuf.StringValue;

public interface GridFs {
    StringValue storeOne(MediaValue request);
}
