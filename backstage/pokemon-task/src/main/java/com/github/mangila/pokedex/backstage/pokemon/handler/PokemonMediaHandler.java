package com.github.mangila.pokedex.backstage.pokemon.handler;

import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.Cries;
import com.github.mangila.pokedex.backstage.integration.pokeapi.response.pokemon.sprites.Sprites;
import org.springframework.stereotype.Service;

@Service
public class PokemonMediaHandler {

    private final RedisBouncerClient redisBouncerClient;

    public PokemonMediaHandler(RedisBouncerClient redisBouncerClient) {
        this.redisBouncerClient = redisBouncerClient;
    }

    public void handle(Sprites sprites) {

    }

    public void handle(Cries cries) {

    }
}
