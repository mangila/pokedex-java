package com.github.mangila.pokedex.shared.database.internal.write;

import com.github.mangila.pokedex.shared.model.Pokemon;

import java.util.concurrent.CompletableFuture;

public record WriteTransfer(String key, Pokemon pokemon, CompletableFuture<Long> result) {
}
