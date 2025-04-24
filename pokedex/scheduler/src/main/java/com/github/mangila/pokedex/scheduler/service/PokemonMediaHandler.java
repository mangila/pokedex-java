package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.pokeapi.response.pokemon.Cries;
import com.github.mangila.pokedex.shared.pokeapi.response.pokemon.sprites.Sprites;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PokemonMediaHandler {

    private static final Logger logger = LoggerFactory.getLogger(PokemonMediaHandler.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public PokemonMediaHandler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void queueMedia(Pair<Integer, Integer> idPair,
                           String name,
                           Sprites sprites,
                           Cries cries) {
        logger.info("Queueing media for Pokemon: id={}, name={}", idPair, name);
        queueIfUrlNotNull(idPair, name, "front-default", sprites.frontDefault());
        queueIfUrlNotNull(idPair, name, "offical-artwork-front-default", sprites.other().officialArtwork().frontDefault());
        queueIfUrlNotNull(idPair, name, "cries-legacy", cries.legacy());
        queueIfUrlNotNull(idPair, name, "cries-latest", cries.latest());
        logger.debug("Media queuing completed for Pokemon: {}", name);
    }

    private void queueIfUrlNotNull(Pair<Integer, Integer> idPair,
                                   String name,
                                   String suffix,
                                   String url) {
        if (Objects.nonNull(url)) {
            logger.debug("Adding media to queue: type={}, pokemon={}", suffix, name);
            MediaEntry entry = new MediaEntry(
                    idPair.getFirst(),
                    idPair.getSecond(),
                    name,
                    suffix,
                    PokeApiUri.create(url)
            );
            redisTemplate.opsForSet().add(QueueService.MEDIA_QUEUE, entry);
            logger.trace("Media entry added: {}", entry);
        } else {
            logger.warn("Skipping null URL for media type: {}, pokemon: {}", suffix, name);
        }
    }
}
