package com.github.mangila.scheduler.service;

import com.github.mangila.model.domain.PokemonMedia;
import com.github.mangila.scheduler.config.CacheConfig;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;

@Service
public class GridFsService {

    private final GridFsTemplate gridFsTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestClient http;

    public GridFsService(GridFsTemplate gridFsTemplate,
                         RedisTemplate<String, Object> redisTemplate,
                         @Qualifier("pokemon-media-client") RestClient http) {
        this.gridFsTemplate = gridFsTemplate;
        this.redisTemplate = redisTemplate;
        this.http = http;
    }

    @SneakyThrows
    @Cacheable(value = CacheConfig.POKE_API_MEDIA, key = "#pokemonMedia.buildFileName()")
    public String store(PokemonMedia pokemonMedia) {
        var value = http.get()
                .uri(pokemonMedia.url().toURI())
                .retrieve()
                .body(byte[].class);
        var id = gridFsTemplate.store(new ByteArrayInputStream(value),
                pokemonMedia.buildFileName(),
                pokemonMedia.createContentType());
        return id.toString();
    }

}
