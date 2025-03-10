package com.github.mangila.pokedex.backstage.media.task;

import com.github.mangila.pokedex.backstage.model.grpc.model.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.bouncer.mongo.MongoBouncerClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.util.Map;

@Service
public class MediaTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(MediaTask.class);

    private final MongoBouncerClient mongoBouncerClient;
    private final RedisBouncerClient redisBouncerClient;

    public MediaTask(MongoBouncerClient mongoBouncerClient,
                     RedisBouncerClient redisBouncerClient) {
        this.mongoBouncerClient = mongoBouncerClient;
        this.redisBouncerClient = redisBouncerClient;
    }

    @Override
    public void run(String[] args) {
        var message = redisBouncerClient.streamOps()
                .readOne(StreamRecord.newBuilder()
                        .setStreamKey(RedisStreamKey.POKEMON_MEDIA_EVENT.getKey())
                        .build());
        var data = message.getDataMap();
        if (CollectionUtils.isEmpty(data)) {
            log.debug("No new messages found");
            return;
        }
        var url = data.get("url");
        ensureUrlDeriveFromPokeApi(url);
        var response = RestClient.create(url)
                .get()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .retrieve()
                .toEntity(byte[].class);
        if (response.getStatusCode().is2xxSuccessful()) {
            var pokemonName = data.get("pokemon_name");
            var speciesId = data.get("species_id");
            var pokemonId = data.get("pokemon_id");
            var description = data.get("description");
            var fileName = createFileName(data);
        }
    }

    private void ensureUrlDeriveFromPokeApi(String url) {
        var isFromPokeApi = url.startsWith("https://raw.githubusercontent.com/PokeAPI");
        if (!isFromPokeApi) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }

    private String createFileName(Map<String, String> data) {
        var pokemonName = data.get("pokemon_name");
        var description = data.get("description");
        var url = data.get("url");
        return new StringBuilder()
                .append(pokemonName)
                .append("-")
                .append(description)
                .append(".")
                .append(UriUtils.extractFileExtension(url))
                .toString();
    }
}