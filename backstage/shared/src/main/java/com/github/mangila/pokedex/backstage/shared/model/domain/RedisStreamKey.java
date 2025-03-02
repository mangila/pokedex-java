package com.github.mangila.pokedex.backstage.shared.model.domain;

public enum RedisStreamKey {

    POKEMON_NAME_EVENT("pokemon-name-event"),
    POKEMON_MEDIA_EVENT("pokemon-media-event");

    private final String key;

    RedisStreamKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

