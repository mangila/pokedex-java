package com.github.mangila.pokedex.database.model;

public record CallbackItem<T>(T value, WriteCallback callback) {
}
