package com.github.mangila.scheduler.service;

import com.github.mangila.model.domain.PokemonMedia;
import com.github.mangila.repository.MongoTemplateRepository;
import com.github.mangila.repository.PokemonSpeciesRepository;
import com.github.mangila.repository.document.PokemonSpeciesDocument;
import com.github.mangila.scheduler.mapper.PokeApiMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MongoDbService {

    private final PokemonSpeciesRepository pokemonSpeciesRepository;
    private final MongoTemplateRepository mongoTemplateRepository;
    private final PokeApiMapper pokeApiMapper;

    public void save(PokemonSpeciesDocument pokemonSpeciesDocument) {
        pokemonSpeciesRepository.save(pokemonSpeciesDocument);
    }

    public void saveImageToVariety(String mediaId, PokemonMedia image) {
        mongoTemplateRepository.saveImageToVariety(
                Pair.of(image.speciesId(), image.varietyId()),
                pokeApiMapper.ToImageDocument(mediaId, image)
        );
    }

    public void saveAudioToVariety(String mediaId, PokemonMedia audio) {
        mongoTemplateRepository.saveAudioToVariety(
                Pair.of(audio.speciesId(), audio.varietyId()),
                pokeApiMapper.ToImageDocument(mediaId, audio)
        );
    }
}
