package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.github.mangila.pokedex.backstage.model.grpc.redis.EntryRequest;
import com.google.protobuf.Empty;

import java.util.Optional;

public interface ValueOperation {

    Empty set(EntryRequest request);

    Optional<String> get(EntryRequest request);
}
