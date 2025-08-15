package com.github.mangila.pokedex.database.internal.read;

import java.util.concurrent.CompletableFuture;

public record ReadTransfer(String key, CompletableFuture<byte[]> result) {
}
