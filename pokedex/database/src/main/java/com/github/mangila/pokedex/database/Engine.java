package com.github.mangila.pokedex.database;

import java.util.concurrent.CompletableFuture;

public interface Engine {

    CompletableFuture<Boolean> putAsync(String key, String field, byte[] value);
}
