package com.github.mangila.pokedex.database.internal.io.model;

import java.util.concurrent.CompletableFuture;

public record WriteOperation(Key key,
                             Value value,
                             CompletableFuture<Boolean> result) {
}
