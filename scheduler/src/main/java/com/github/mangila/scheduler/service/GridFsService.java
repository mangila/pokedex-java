package com.github.mangila.scheduler.service;

import com.github.mangila.model.domain.PokemonMedia;
import com.github.mangila.repository.GridFsRepository;
import com.github.mangila.scheduler.config.CacheConfig;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;

@Service
public class GridFsService {

    private final RestClient http;
    private final GridFsRepository gridFsRepository;

    public GridFsService(GridFsRepository gridFsRepository,
                         @Qualifier("pokemon-media-client") RestClient http) {
        this.gridFsRepository = gridFsRepository;
        this.http = http;
    }

    @SneakyThrows({URISyntaxException.class})
    @Cacheable(value = CacheConfig.POKE_API_MEDIA, key = "#pokemonMedia.buildFileName()")
    public String store(PokemonMedia pokemonMedia) {
        var value = http.get()
                .uri(pokemonMedia.url().toURI())
                .retrieve()
                .body(byte[].class);
        var id = gridFsRepository.store(new ByteArrayInputStream(value),
                pokemonMedia.buildFileName(),
                pokemonMedia.createContentType());
        return id.toString();
    }

}
