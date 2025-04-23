package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.PokemonEntry;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.shared.pokeapi.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.shared.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.pokedex.shared.pokeapi.response.species.SpeciesResponse;
import com.github.mangila.pokedex.shared.repository.PokemonSpeciesRepository;
import com.github.mangila.pokedex.shared.repository.document.PokemonSpeciesDocument;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@lombok.AllArgsConstructor
@lombok.extern.slf4j.Slf4j
public class PokemonTask {

    private final PokeApiTemplate pokeApiTemplate;
    private final PokemonSpeciesRepository pokemonSpeciesRepository;
    private final PokemonMediaHandler pokemonMediaHandler;

    /**
     * Processes a given PokemonEntry by retrieving relevant species, evolution chain, and varieties information
     * from the PokeApi, constructing a PokemonSpeciesDocument, saving it into the repository, and queuing related media
     * for processing.
     *
     * @param pokemonEntry the Pokemon entry containing the name and PokeApiUri used as the entry point for fetching species data
     */
    public void run(PokemonEntry pokemonEntry) {
        var speciesResponse = pokeApiTemplate.fetchByUrl(pokemonEntry.pokeApiUri(), SpeciesResponse.class);
        var evolutionChainResponse = pokeApiTemplate.fetchByUrl(PokeApiUri.create(speciesResponse.evolutionChain().url()), EvolutionChainResponse.class);
        var varieties = speciesResponse.varieties()
                .stream()
                .peek(variety -> log.debug("Processing varieties: {} - {}", variety.pokemon().name(), pokemonEntry.name()))
                .map(variety -> variety.pokemon().url())
                .map(PokeApiUri::create)
                .map(uri -> pokeApiTemplate.fetchByUrl(uri, PokemonResponse.class))
                .toList();
        var document = PokemonSpeciesDocument.fromSpeciesEvolutionAndVarieties(speciesResponse, evolutionChainResponse, varieties);
        pokemonSpeciesRepository.save(document);
        varieties.forEach(variety -> pokemonMediaHandler.queueMedia(Pair.of(speciesResponse.id(), variety.id()),
                variety.name(),
                variety.sprites(),
                variety.cries()));
    }

}
