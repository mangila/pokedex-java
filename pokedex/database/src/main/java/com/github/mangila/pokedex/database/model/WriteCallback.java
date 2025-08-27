package com.github.mangila.pokedex.database.model;

import java.util.concurrent.CompletableFuture;

public record WriteCallback(CompletableFuture<Void> future) {

    public static WriteCallback newCallback() {
        return new WriteCallback(new CompletableFuture<>());
    }

}
