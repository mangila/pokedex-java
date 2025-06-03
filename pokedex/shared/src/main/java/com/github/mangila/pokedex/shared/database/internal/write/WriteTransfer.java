package com.github.mangila.pokedex.shared.database.internal.write;

import java.util.concurrent.CompletableFuture;

public record WriteTransfer(String key,
                            byte[] value,
                            CompletableFuture<Boolean> result) {
}
