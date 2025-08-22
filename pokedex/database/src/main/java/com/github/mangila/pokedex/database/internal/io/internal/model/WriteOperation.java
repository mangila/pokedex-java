package com.github.mangila.pokedex.database.internal.io.internal.model;

import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;

import java.util.concurrent.CompletableFuture;

public record WriteOperation(Key key,
                             Value value,
                             Operation operation,
                             CompletableFuture<Boolean> result) {

    public enum Operation {
        WRITE,
        TRUNCATE,
        DELETE
    }

}
