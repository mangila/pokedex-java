package com.github.mangila.pokedex.database;

import com.github.mangila.pokedex.database.model.WriteCallback;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

public sealed interface Database permits DefaultDatabase {

    void open();

    boolean isOpen();

    void close();

    void flush();

    String getString(String key, String field);

    CompletableFuture<String> getStringAsync(String key, String field);

    WriteCallback put(String key, String field, byte[] value);

    CompletableFuture<WriteCallback> putAsync(String key, String field, byte[] value);

    CompletableFuture<WriteCallback> putAsync(String key, String field, String value);

    CompletableFuture<WriteCallback> putAsync(String key, String field, Boolean value);

    CompletableFuture<WriteCallback> putAsync(String key, String field, BigInteger value);
}
