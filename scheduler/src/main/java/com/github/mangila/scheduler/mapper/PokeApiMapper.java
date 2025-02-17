package com.github.mangila.scheduler.mapper;

import com.github.mangila.integration.pokeapi.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.integration.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.integration.pokeapi.response.species.PokemonSpeciesResponse;
import com.github.mangila.model.domain.PokemonMedia;
import com.github.mangila.repository.document.PokemonSpeciesDocument;
import com.github.mangila.repository.document.embedded.PokemonMediaDocument;
import com.github.mangila.repository.document.embedded.PokemonVarietyDocument;
import com.github.mangila.scheduler.config.FileServerProperties;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.yaml.snakeyaml.util.UriEncoder;

import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class PokeApiMapper {

    private final FileServerProperties fileServerProperties;

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
                    document.setVarietyName(pokemonResponse.name());
                    document.setIsDefault(pokemonResponse.isDefault());
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
        var src = UriComponentsBuilder.newInstance()
                .scheme(fileServerProperties.getScheme())
                .host(fileServerProperties.getHost())
                .port(fileServerProperties.getPort())
                .path(fileServerProperties.getUri())
                .build(image.buildFileName());
        document.setSrc(UriEncoder.decode(src.toString()));
        return document;
    }
}
