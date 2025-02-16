package com.github.mangila.scheduler.service;

import com.github.mangila.model.domain.PokemonName;
import lombok.Locked;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

    private static final String POKEMON_NAME_QUEUE = "pokemon-name-queue";
    private final RedisTemplate<String, Object> redisTemplate;

    public QueueService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Locked.Write
    public void push(PokemonName pokemonName) {
        redisTemplate.opsForList()
                .rightPush(POKEMON_NAME_QUEUE, pokemonName);
    }

    @Locked.Read
    public PokemonName pop() {
        return (PokemonName) redisTemplate.opsForList()
                .leftPop(POKEMON_NAME_QUEUE);
    }

}
