package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.scheduler.repository.document.PokemonSpeciesDocument;
import com.github.mangila.pokedex.scheduler.repository.document.embedded.PokemonMediaDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Objects;

@Service
public class MediaTask {

    private final GridFsTemplate gridFsTemplate;
    private final MongoTemplate mongoTemplate;
    private final RestClient http;

    public MediaTask(GridFsTemplate gridFsTemplate,
                     MongoTemplate mongoTemplate) {
        this.gridFsTemplate = gridFsTemplate;
        this.mongoTemplate = mongoTemplate;
        this.http = RestClient.builder()
                .requestFactory(new ReactorClientHttpRequestFactory())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .defaultHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }


    public void run(MediaEntry mediaEntry) {
        var response = http.get()
                .uri(mediaEntry.uri())
                .retrieve()
                .toEntity(byte[].class);
        if (response.getStatusCode().is2xxSuccessful() && Objects.nonNull(response.getBody())) {
            var fileName = createFileName(
                    mediaEntry.name(),
                    mediaEntry.suffix(),
                    mediaEntry.uri()
            );
            var contentType = getContentType(fileName);
            var mediaId = gridFsTemplate.store(
                    new ByteArrayInputStream(response.getBody()),
                    fileName,
                    contentType);
            var query = Query.query(Criteria.where("_id")
                    .is(mediaEntry.speciesId())
                    .and("varieties.pokemon_id")
                    .is(mediaEntry.varietyId()));
            Update update = new Update();
            update.push("varieties.$.media", PokemonMediaDocument.builder()
                    .mediaId(mediaId.toString())
                    .fileName(fileName)
                    .build());
            mongoTemplate.updateFirst(query, update, PokemonSpeciesDocument.class);
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

    private String createFileName(String name, String suffix, URI uri) {
        return new StringBuilder()
                .append(name)
                .append("-")
                .append(suffix)
                .append(".")
                .append(UriUtils.extractFileExtension(uri.toString()))
                .toString();
    }
}
