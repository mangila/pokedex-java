package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Entry;

import java.util.concurrent.CompletableFuture;

public sealed interface WalManager permits DefaultWalManager {

    void open();

    void close();

    CompletableFuture<Void> putAsync(Entry entry);

}
