package com.github.mangila.pokedex.backstage.pokemon.task;

import com.github.mangila.pokedex.backstage.cache.config.RedisQueue;
import com.github.mangila.pokedex.backstage.cache.service.QueueService;
import com.github.mangila.pokedex.backstage.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PokemonTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(PokemonTask.class);
    private final QueueService queueService;

    public PokemonTask(QueueService queueService) {
        this.queueService = queueService;
    }

    @Override
    public void run(String[] args) {
        var pokemonName = queueService.popAsString(RedisQueue.GENERATION_QUEUE.toString());
        log.info("Pokemon Name: {}", pokemonName);
    }
}
