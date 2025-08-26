package com.github.mangila.pokedex.database.wal;

import com.github.mangila.pokedex.database.model.Field;
import com.github.mangila.pokedex.database.model.Key;
import com.github.mangila.pokedex.database.model.Value;

import java.util.concurrent.CompletableFuture;

public sealed interface WalManager permits DefaultWalManager {

    void open();

    void close();

    CompletableFuture<Void> putAsync(Key key, Field field, Value value);

}
