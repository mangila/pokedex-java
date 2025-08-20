package com.github.mangila.pokedex.database.internal.io.model;

import java.util.concurrent.CompletableFuture;

public record ReadOperation(Key key, CompletableFuture<Value> result) {
}
