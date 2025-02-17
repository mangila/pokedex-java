package com.github.mangila.scheduler.service;

import com.github.mangila.repository.document.PokemonSpeciesDocument;
import com.github.mangila.model.domain.PokemonMedia;
import com.github.mangila.repository.PokemonSpeciesRepository;
import com.github.mangila.scheduler.mapper.PokeApiMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MongoDbService {

    private final PokemonSpeciesRepository pokemonSpeciesRepository;
    private final PokeApiMapper pokeApiMapper;
    private final MongoTemplate mongoTemplate;

    public void save(PokemonSpeciesDocument pokemonSpeciesDocument) {
        pokemonSpeciesRepository.save(pokemonSpeciesDocument);
    }

    public void saveImageToVariety(String mediaId, PokemonMedia image) {
        var speciesId = image.speciesId();
        var varietyId = image.varietyId();
        var q = Query.query(Criteria.where("_id")
                .is(speciesId.toInteger())
                .and("varieties.variety_id")
                .is(varietyId.toInteger()));
        Update update = new Update();
        update.push("varieties.$.images", pokeApiMapper.ToImageDocument(mediaId, image));
        mongoTemplate.updateFirst(q, update, PokemonSpeciesDocument.class);
    }

    public void saveAudioToVariety(String mediaId, PokemonMedia audio) {
        var speciesId = audio.speciesId();
        var varietyId = audio.varietyId();
        var q = Query.query(Criteria.where("_id")
                .is(speciesId.toInteger())
                .and("varieties.variety_id")
                .is(varietyId.toInteger()));
        Update update = new Update();
        update.push("varieties.$.audios", pokeApiMapper.ToImageDocument(mediaId, audio));
        mongoTemplate.updateFirst(q, update, PokemonSpeciesDocument.class);
    }
}
