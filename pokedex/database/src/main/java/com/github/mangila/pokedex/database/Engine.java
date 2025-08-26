package com.github.mangila.pokedex.database;

import java.util.concurrent.CompletableFuture;

public sealed interface Engine permits DefaultEngine {

    boolean isOpen();

    void open();

    void close();

    CompletableFuture<Void> putAsync(String key, String field, byte[] value);
}
