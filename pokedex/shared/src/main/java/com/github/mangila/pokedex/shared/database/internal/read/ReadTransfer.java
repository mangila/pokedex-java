package com.github.mangila.pokedex.shared.database.internal.read;

import java.util.concurrent.CompletableFuture;

public record ReadTransfer(String key, CompletableFuture<byte[]> result) {
}
