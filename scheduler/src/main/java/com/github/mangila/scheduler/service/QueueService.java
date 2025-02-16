package com.github.mangila.scheduler.service;

import com.github.mangila.model.domain.PokemonMedia;
import com.github.mangila.model.domain.PokemonName;
import lombok.AllArgsConstructor;
import lombok.Locked;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class QueueService {

    public static final String POKEMON_NAME_QUEUE = "pokemon-name-queue";
    public static final String POKEMON_IMAGE_QUEUE = "pokemon-image-queue";
    public static final String POKEMON_AUDIO_QUEUE = "pokemon-audio-queue";
    private final RedisTemplate<String, Object> redisTemplate;

    @Locked.Write
    public void pushNameQueue(PokemonName pokemonName) {
        redisTemplate.opsForSet()
                .add(POKEMON_NAME_QUEUE, pokemonName);
    }

    @Locked.Read
    public PokemonName popNameQueue() {
        return (PokemonName) redisTemplate.opsForSet()
                .pop(POKEMON_NAME_QUEUE);
    }

    @Locked.Write
    public void pushImageQueue(PokemonMedia media) {
        redisTemplate.opsForSet()
                .add(POKEMON_IMAGE_QUEUE, media);
    }

    @Locked.Read
    public PokemonMedia popImageQueue() {
        return (PokemonMedia) redisTemplate.opsForSet()
                .pop(POKEMON_IMAGE_QUEUE);
    }

    @Locked.Write
    public void pushAudioQueue(PokemonMedia media) {
        redisTemplate.opsForSet()
                .add(POKEMON_AUDIO_QUEUE, media);
    }

    @Locked.Read
    public PokemonMedia popAudioQueue() {
        return (PokemonMedia) redisTemplate.opsForSet()
                .pop(POKEMON_AUDIO_QUEUE);
    }

}
