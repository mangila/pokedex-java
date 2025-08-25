package com.github.mangila.pokedex.database.model;

import java.util.concurrent.CompletableFuture;

public record CallbackItem<T>(T value, CompletableFuture<Void> callback) {
}
