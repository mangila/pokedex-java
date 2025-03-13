package com.github.mangila.pokedex.backstage.media.task;

import com.github.mangila.pokedex.backstage.media.props.MediaTaskProperties;
import com.github.mangila.pokedex.backstage.model.grpc.model.MediaValue;
import com.github.mangila.pokedex.backstage.model.grpc.model.PokemonMediaValue;
import com.github.mangila.pokedex.backstage.model.grpc.model.StreamRecord;
import com.github.mangila.pokedex.backstage.shared.bouncer.imageconverter.ImageConverterClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.mongo.MongoBouncerClient;
import com.github.mangila.pokedex.backstage.shared.bouncer.redis.RedisBouncerClient;
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

    private final MediaTaskProperties taskProperties;
    private final ImageConverterClient imageConverterClient;
    private final MongoBouncerClient mongoBouncerClient;
    private final RedisBouncerClient redisBouncerClient;

    public MediaTask(MediaTaskProperties taskProperties,
                     ImageConverterClient imageConverterClient,
                     MongoBouncerClient mongoBouncerClient,
                     RedisBouncerClient redisBouncerClient) {
        this.taskProperties = taskProperties;
        this.imageConverterClient = imageConverterClient;
        this.mongoBouncerClient = mongoBouncerClient;
        this.redisBouncerClient = redisBouncerClient;
    }

    /**
     * 0. Read from pokemon-media-event stream
     * 1. download media from PokeAPI
     * 2. if media download was successful - else leave message pending
     * 3. convert media to webp if possible
     * 4. store media to GridFs
     * 5. save media src(url to file-api) and file name to mongodb
     * 6. acknowledge message to stream as successful
     */
    @Override
    public void run() {
        var streamRecord = StreamRecord.newBuilder()
                .setStreamKey(taskProperties.getMediaStreamKey().getKey())
                .build();
        var message = redisBouncerClient.streamOps().readOne(streamRecord);
        var data = message.getDataMap();
        if (CollectionUtils.isEmpty(data)) {
            log.debug("No new messages");
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
            var mediaId = mongoBouncerClient.gridFs().storeOne(converted);
            var speciesId = data.get("species_id");
            var pokemonId = data.get("pokemon_id");
            mongoBouncerClient.mongoDb().pushMedia(PokemonMediaValue.newBuilder()
                    .setMediaId(mediaId.getValue())
                    .setSpeciesId(Integer.parseInt(speciesId))
                    .setPokemonId(Integer.parseInt(pokemonId))
                    .setFileName(fileName)
                    .build());
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