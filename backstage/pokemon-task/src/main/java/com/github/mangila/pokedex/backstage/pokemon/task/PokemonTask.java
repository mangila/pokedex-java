package com.github.mangila.pokedex.backstage.pokemon.task;

import com.github.mangila.pokedex.backstage.integration.bouncer.mongodb.MongoDbBouncerClient;
import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PokemonTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(PokemonTask.class);

    private final PokeApiTemplate pokeApiTemplate;
    private final MongoDbBouncerClient mongoDbBouncerClient;
    private final RedisBouncerClient redisBouncerClient;

    public PokemonTask(PokeApiTemplate pokeApiTemplate,
                       MongoDbBouncerClient mongoDbBouncerClient,
                       RedisBouncerClient redisBouncerClient) {
        this.pokeApiTemplate = pokeApiTemplate;
        this.mongoDbBouncerClient = mongoDbBouncerClient;
        this.redisBouncerClient = redisBouncerClient;
    }


    @Override
    public void run(String[] args) {
        log.info("Starting Pokemon Task");
    }
}
