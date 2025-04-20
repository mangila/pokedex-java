package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.scheduler.repository.document.PokemonSpeciesDocument;
import com.github.mangila.pokedex.scheduler.repository.document.embedded.PokemonMediaDocument;
import com.github.mangila.pokedex.scheduler.util.SchedulerUtils;
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

import java.io.ByteArrayInputStream;
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
                .build();
    }


    /**
     * Executes a task to retrieve media from a specified URI, store it in GridFS with metadata,
     * and update the media information in the corresponding database document for a specific Pokemon species and variety.
     *
     * @param mediaEntry the media entry object containing details about the species, variety, name, file suffix, and URI to the media
     */
    public void run(MediaEntry mediaEntry) {
        var uri = mediaEntry.uri();
        SchedulerUtils.ensureUriFromPokeApi(uri);
        var response = http.get()
                .uri(uri)
                .retrieve()
                .toEntity(byte[].class);
        if (response.getStatusCode().is2xxSuccessful() && Objects.nonNull(response.getBody())) {
            var fileName = SchedulerUtils.createFileName(
                    mediaEntry.name(),
                    mediaEntry.suffix(),
                    uri
            );
            var contentType = SchedulerUtils.getContentType(fileName);
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
}
