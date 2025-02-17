package com.github.mangila.repository;

import com.github.mangila.model.domain.PokemonId;
import com.github.mangila.repository.document.PokemonSpeciesDocument;
import com.github.mangila.repository.document.embedded.PokemonMediaDocument;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MongoTemplateRepository {

    private final MongoTemplate mongoTemplate;

    public void saveImageToVariety(Pair<PokemonId, PokemonId> idPair, PokemonMediaDocument document) {
        var speciesId = idPair.getFirst();
        var varietyId = idPair.getSecond();
        var q = Query.query(Criteria.where("_id")
                .is(speciesId.toInteger())
                .and("varieties.variety_id")
                .is(varietyId.toInteger()));
        Update update = new Update();
        update.push("varieties.$.images", document);
        mongoTemplate.updateFirst(q, update, PokemonSpeciesDocument.class);
    }

    public void saveAudioToVariety(Pair<PokemonId, PokemonId> idPair, PokemonMediaDocument document) {
        var speciesId = idPair.getFirst();
        var varietyId = idPair.getSecond();
        var q = Query.query(Criteria.where("_id")
                .is(speciesId.toInteger())
                .and("varieties.variety_id")
                .is(varietyId.toInteger()));
        Update update = new Update();
        update.push("varieties.$.audios", document);
        mongoTemplate.updateFirst(q, update, PokemonSpeciesDocument.class);
    }
}
