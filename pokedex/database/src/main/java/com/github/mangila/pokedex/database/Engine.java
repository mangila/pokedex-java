package com.github.mangila.pokedex.database;

import java.util.concurrent.CompletableFuture;

public sealed interface Engine permits DefaultEngine {

    CompletableFuture<Void> putAsync(String key, String field, byte[] value);
}
