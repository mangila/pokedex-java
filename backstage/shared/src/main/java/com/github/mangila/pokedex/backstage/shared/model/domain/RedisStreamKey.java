package com.github.mangila.pokedex.backstage.shared.model.domain;

public enum RedisStreamKey {

    POKEMON_NAME_EVENT("pokemon-name-event"),
    POKEMON_NAME_DEAD_EVENT("pokemon-name-dead-event"),
    POKEMON_MEDIA_EVENT("pokemon-media-event"),
    POKEMON_MEDIA_DEAD_EVENT("pokemon-media-dead-event");

    private final String key;

    RedisStreamKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

