package com.github.mangila.pokedex.shared.database.internal.write;

import java.util.concurrent.CompletableFuture;

public record WriteTransfer<V>(String key, V value, CompletableFuture<Integer> result) {
}
