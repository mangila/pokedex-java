package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.pokeapi.PokeApiMediaTemplate;
import com.github.mangila.pokedex.shared.repository.document.PokemonSpeciesDocument;
import com.github.mangila.pokedex.shared.repository.document.embedded.PokemonMediaDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.ByteArrayInputStream;

@Service
public class MediaTask {

    private final GridFsTemplate gridFsTemplate;
    private final MongoTemplate mongoTemplate;
    private final PokeApiMediaTemplate http;

    public MediaTask(GridFsTemplate gridFsTemplate,
                     MongoTemplate mongoTemplate,
                     PokeApiMediaTemplate http) {
        this.gridFsTemplate = gridFsTemplate;
        this.mongoTemplate = mongoTemplate;
        this.http = http;
    }

    /**
     * Executes the media processing task including fetching, storing, and updating related data
     * in the database to associate media with the corresponding Pokémon species and variety.
     *
     * @param mediaEntry the media entry containing the necessary information such as species ID,
     *                   variety ID, media name, suffix, and URI for the media resource.
     */
    public void run(MediaEntry mediaEntry) {
        var uri = mediaEntry.uri();
        var response = http.fetchMedia(uri);
        var fileName = createFileName(
                mediaEntry.name(),
                mediaEntry.suffix(),
                uri
        );
        var mediaId = gridFsTemplate.store(
                new ByteArrayInputStream(response.imageData()),
                fileName,
                response.mediaType().getType());
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

    private String createFileName(String name, String suffix, PokeApiUri pokeApiUri) {
        return new StringBuilder()
                .append(name)
                .append("-")
                .append(suffix)
                .append(".")
                .append(UriUtils.extractFileExtension(pokeApiUri.toUriString()))
                .toString();
    }
}
