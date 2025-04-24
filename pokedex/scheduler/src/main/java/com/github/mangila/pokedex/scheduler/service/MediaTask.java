package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.pokeapi.PokeApiMediaTemplate;
import com.github.mangila.pokedex.shared.repository.document.PokemonSpeciesDocument;
import com.github.mangila.pokedex.shared.repository.document.embedded.PokemonMediaDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(MediaTask.class);
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
        logger.info("Processing media entry: name={}, suffix={}, speciesId={}, varietyId={}", 
                mediaEntry.name(), mediaEntry.suffix(), mediaEntry.speciesId(), mediaEntry.varietyId());

        var uri = mediaEntry.uri();
        logger.debug("Fetching media from URI: {}", uri);
        var response = http.fetchMedia(uri);
        logger.debug("Media fetched successfully: contentType={}, contentLength={}", 
                response.mediaType(), response.contentLength());

        var fileName = createFileName(
                mediaEntry.name(),
                mediaEntry.suffix(),
                uri
        );
        logger.debug("Created filename: {}", fileName);

        logger.debug("Storing media in GridFS: fileName={}, contentType={}", 
                fileName, response.mediaType().getType());
        var mediaId = gridFsTemplate.store(
                new ByteArrayInputStream(response.imageData()),
                fileName,
                response.mediaType().getType());
        logger.debug("Media stored in GridFS with ID: {}", mediaId);

        logger.debug("Creating query to update Pokemon document: speciesId={}, varietyId={}", 
                mediaEntry.speciesId(), mediaEntry.varietyId());
        var query = Query.query(Criteria.where("_id")
                .is(mediaEntry.speciesId())
                .and("varieties.pokemon_id")
                .is(mediaEntry.varietyId()));

        Update update = new Update();
        var mediaDocument = new PokemonMediaDocument(
                mediaId.toString(),
                fileName
        );
        update.push("varieties.$.media", mediaDocument);

        logger.debug("Updating MongoDB document with media information");
        var result = mongoTemplate.updateFirst(query, update, PokemonSpeciesDocument.class);
        logger.info("MongoDB update completed: matched={}, modified={}", 
                result.getMatchedCount(), result.getModifiedCount());

        logger.info("Media processing completed for: {}-{}", mediaEntry.name(), mediaEntry.suffix());
    }

    private String createFileName(String name, String suffix, PokeApiUri pokeApiUri) {
        logger.trace("Creating filename from: name={}, suffix={}, uri={}", name, suffix, pokeApiUri);
        String extension = UriUtils.extractFileExtension(pokeApiUri.toUriString());
        logger.trace("Extracted file extension: {}", extension);

        String fileName = new StringBuilder()
                .append(name)
                .append("-")
                .append(suffix)
                .append(".")
                .append(extension)
                .toString();

        logger.trace("Created filename: {}", fileName);
        return fileName;
    }
}
