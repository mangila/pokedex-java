package com.github.mangila.pokedex.database.internal.io.internal.model;

import com.github.mangila.pokedex.database.internal.model.Key;
import com.github.mangila.pokedex.database.internal.model.Value;

import java.util.concurrent.CompletableFuture;

public record ReadOperation(Key key, CompletableFuture<Value> result) {
}
