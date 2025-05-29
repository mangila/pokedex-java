package com.github.mangila.pokedex.shared.database.internal.read;

import com.github.mangila.pokedex.shared.model.Pokemon;

import java.util.concurrent.CompletableFuture;

public record ReadTransfer(String key, CompletableFuture<Pokemon> result) {
}
