package com.github.mangila.pokedex.backstage.image.task;

import com.github.mangila.pokedex.backstage.integration.bouncer.mongodb.MongoDbBouncerClient;
import com.github.mangila.pokedex.backstage.integration.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.integration.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ImageTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(ImageTask.class);

    private final PokeApiTemplate pokeApiTemplate;
    private final MongoDbBouncerClient mongoDbBouncerClient;
    private final RedisBouncerClient redisBouncerClient;

    public ImageTask(PokeApiTemplate pokeApiTemplate,
                     MongoDbBouncerClient mongoDbBouncerClient,
                     RedisBouncerClient redisBouncerClient) {
        this.pokeApiTemplate = pokeApiTemplate;
        this.mongoDbBouncerClient = mongoDbBouncerClient;
        this.redisBouncerClient = redisBouncerClient;
    }

    @Override
    public void run(String[] args) {
        log.info("Starting Image Task");
    }
}
