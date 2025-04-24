package com.github.mangila.pokedex.shared.pokeapi;

import com.github.mangila.pokedex.shared.model.PokeApiUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Service
public class PokeApiTemplate {

    private static final Logger log = LoggerFactory.getLogger(PokeApiTemplate.class);
    private final RestClient http;
    private final RedisTemplate<String, Object> redisTemplate;

    public PokeApiTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.http = RestClient.builder()
                .requestFactory(new ReactorClientHttpRequestFactory())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .build();
        this.redisTemplate = redisTemplate;
    }

    public <T> T fetchByUrl(PokeApiUri pokeApiUri, Class<T> clazz) {
        var cacheValue = redisTemplate.opsForValue().get(pokeApiUri.toUriString());
        if (Objects.nonNull(cacheValue)) {
            log.debug("Cache hit - {}", pokeApiUri);
            return clazz.cast(cacheValue);
        }
        log.debug("Cache miss - {}", pokeApiUri);
        var response = http.get()
                .uri(pokeApiUri.uri())
                .retrieve()
                .body(clazz);
        if (Objects.nonNull(response)) {
            redisTemplate.opsForValue()
                    .set(pokeApiUri.toUriString(), response);
        }
        return response;
    }
}
