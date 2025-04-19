package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.scheduler.pokeapi.response.pokemon.Cries;
import com.github.mangila.pokedex.scheduler.pokeapi.response.pokemon.sprites.Sprites;
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
        queueIfUrlNotNull(idPair.getFirst(), idPair.getSecond(), name, "front-default", sprites.frontDefault());
        queueIfUrlNotNull(idPair.getFirst(), idPair.getSecond(), name, "offical-artwork-front-default", sprites.other().officialArtwork().frontDefault());
        queueIfUrlNotNull(idPair.getFirst(), idPair.getSecond(), name, "cries-legacy", cries.legacy());
        queueIfUrlNotNull(idPair.getFirst(), idPair.getSecond(), name, "cries-latest", cries.latest());
    }

    private void queueIfUrlNotNull(int speciesId,
                                   int varietyId,
                                   String name,
                                   String suffix,
                                   String url) {
        if (Objects.nonNull(url)) {
            var uri = URI.create(url);
            ensureUriFromPokeApi(uri);
            redisTemplate.opsForSet().add(QueueService.REDIS_POKEMON_MEDIA_SET, MediaEntry.builder()
                    .speciesId(speciesId)
                    .varietyId(varietyId)
                    .name(name)
                    .suffix(suffix)
                    .uri(uri)
                    .build());
        }
    }

    private void ensureUriFromPokeApi(URI uri) {
        if (!uri.toString().startsWith("https://raw.githubusercontent.com/PokeAPI/")) {
            throw new IllegalArgumentException("should start with 'https://raw.githubusercontent.com/PokeAPI/' - " + uri);
        }
    }
}
