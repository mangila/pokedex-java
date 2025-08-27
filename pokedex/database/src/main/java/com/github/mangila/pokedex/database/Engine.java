package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.WriteCallback;

import java.util.concurrent.CompletableFuture;

public sealed interface Engine permits DefaultEngine {

    boolean isOpen();

    void open();

    void close();

    CompletableFuture<WriteCallback> putAsync(String key, String field, byte[] value);
}
