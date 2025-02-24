package com.github.mangila.pokedex.backstage.integration.bouncer.redis;

import com.google.protobuf.Empty;

import java.util.Optional;

public interface ValueOperation {

    Empty set(String key, String value);

    Optional<String> get(String key);
}
