package com.github.mangila.pokedex.backstage.media.task;

import com.github.mangila.pokedex.backstage.model.grpc.model.MediaValue;
import com.github.mangila.pokedex.backstage.model.grpc.model.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.bouncer.imageconverter.ImageConverterClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.mongo.MongoBouncerClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
import com.github.mangila.pokedex.backstage.shared.model.domain.RedisStreamKey;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.util.Map;
import java.util.Objects;

@Service
public class MediaTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(MediaTask.class);

    private final ImageConverterClient imageConverterClient;
    private final MongoBouncerClient mongoBouncerClient;
    private final RedisBouncerClient redisBouncerClient;

    public MediaTask(ImageConverterClient imageConverterClient,
                     MongoBouncerClient mongoBouncerClient,
                     RedisBouncerClient redisBouncerClient) {
        this.imageConverterClient = imageConverterClient;
        this.mongoBouncerClient = mongoBouncerClient;
        this.redisBouncerClient = redisBouncerClient;
    }

    @Override
    public void run(String[] args) {
        var streamRecord = StreamRecord.newBuilder()
                .setStreamKey(RedisStreamKey.POKEMON_MEDIA_EVENT.getKey())
                .build();
        var message = redisBouncerClient.streamOps().readOne(streamRecord);
        var data = message.getDataMap();
        if (CollectionUtils.isEmpty(data)) {
            log.debug("No new messages found");
            return;
        }
        var pokemonName = data.get("pokemon_name");
        var url = data.get("url");
        log.info("Process media - {} - {}", pokemonName, url);
        ensureUrlDeriveFromPokeApi(url);
        var response = RestClient.create(url)
                .get()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .retrieve()
                .toEntity(byte[].class);
        if (response.getStatusCode().is2xxSuccessful() && Objects.nonNull(response.getBody())) {
            var fileName = createFileName(data);
            var contentType = getContentType(fileName);
            var converted = imageConverterClient.convertToWebP(
                    MediaValue.newBuilder()
                            .setFileName(fileName)
                            .setContentType(contentType)
                            .setContent(ByteString.copyFrom(response.getBody()))
                            .build()
            );
            var fileId = mongoBouncerClient.gridFs().storeOne(converted);
            var speciesId = data.get("species_id");
            var pokemonId = data.get("pokemon_id");
            // update mongodb
            redisBouncerClient.streamOps()
                    .acknowledgeOne(streamRecord.toBuilder()
                            .setRecordId(message.getRecordId())
                            .build());
        }
    }

    private void ensureUrlDeriveFromPokeApi(String url) {
        var isFromPokeApi = url.startsWith("https://raw.githubusercontent.com/PokeAPI");
        if (!isFromPokeApi) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }

    private String getContentType(String fileName) {
        return switch (UriUtils.extractFileExtension(fileName)) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "gif" -> MediaType.IMAGE_GIF_VALUE;
            case "svg" -> "image/svg+xml";
            case "ogg" -> "audio/ogg";
            case null -> throw new NullPointerException();
            default -> throw new IllegalArgumentException("Unsupported file extension: " + fileName);
        };
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