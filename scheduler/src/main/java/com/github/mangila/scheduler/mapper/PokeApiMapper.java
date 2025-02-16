package com.github.mangila.scheduler.mapper;

import com.github.mangila.document.PokemonSpeciesDocument;
import com.github.mangila.document.embedded.PokemonMediaDocument;
import com.github.mangila.document.embedded.PokemonVarietyDocument;
import com.github.mangila.integration.pokeapi.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.integration.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.integration.pokeapi.response.species.PokemonSpeciesResponse;
import com.github.mangila.model.domain.PokemonMedia;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PokeApiMapper {

    public PokemonSpeciesDocument ToDocument(PokemonSpeciesResponse speciesResponse,
                                             EvolutionChainResponse evolutionChain,
                                             List<PokemonResponse> varieties) {
        var document = new PokemonSpeciesDocument();
        document.setId(speciesResponse.id());
        document.setName(speciesResponse.name());
        document.setVarieties(ToVarieties(varieties));
        return document;
    }

    private List<PokemonVarietyDocument> ToVarieties(List<PokemonResponse> varieties) {
        return varieties
                .stream()
                .map(pokemonResponse -> {
                    var document = new PokemonVarietyDocument();
                    document.setVarietyId(pokemonResponse.id());
                    document.setName(pokemonResponse.name());
                    document.setImages(Collections.emptyList());
                    document.setAudios(Collections.emptyList());
                    return document;
                }).toList();
    }

    public PokemonMediaDocument ToImageDocument(String mediaId, PokemonMedia image) {
        var document = new PokemonMediaDocument();
        document.setMediaId(mediaId);
        document.setFileName(image.buildFileName());
        document.setContentType(image.createContentType());
        document.setSrc("");
        return document;
    }
}
