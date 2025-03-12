package com.github.mangila.pokedex.backstage.shared.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.model.ValueRequest;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;

import java.util.Optional;

public interface ValueOperation {

    Empty set(ValueRequest request);

    <T extends Message> Optional<T> get(ValueRequest request, Class<T> clazz);
}
