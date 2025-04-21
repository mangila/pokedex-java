package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.shared.pokeapi.response.pokemon.Cries;
import com.github.mangila.pokedex.shared.pokeapi.response.pokemon.sprites.Sprites;
import com.github.mangila.pokedex.shared.util.SchedulerUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Objects;

@Service
@lombok.AllArgsConstructor
@lombok.extern.slf4j.Slf4j
public class PokemonMediaHandler {

    private final RedisTemplate<String, Object> redisTemplate;

    public void queueMedia(Pair<Integer, Integer> idPair,
                           String name,
                           Sprites sprites,
                           Cries cries) {
        queueIfUrlNotNull(idPair, name, "front-default", sprites.frontDefault());
        queueIfUrlNotNull(idPair, name, "offical-artwork-front-default", sprites.other().officialArtwork().frontDefault());
        queueIfUrlNotNull(idPair, name, "cries-legacy", cries.legacy());
        queueIfUrlNotNull(idPair, name, "cries-latest", cries.latest());
    }

    private void queueIfUrlNotNull(Pair<Integer, Integer> idPair,
                                   String name,
                                   String suffix,
                                   String url) {
        if (Objects.nonNull(url)) {
            var uri = URI.create(url);
            SchedulerUtils.ensureUriFromPokeApi(uri);
            redisTemplate.opsForSet().add(QueueService.MEDIA_QUEUE, MediaEntry.builder()
                    .speciesId(idPair.getFirst())
                    .varietyId(idPair.getSecond())
                    .name(name)
                    .suffix(suffix)
                    .uri(uri)
                    .build());
        }
    }
}
