package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.PokemonEntry;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.shared.pokeapi.response.evolutionchain.EvolutionChainResponse;
import com.github.mangila.pokedex.shared.pokeapi.response.pokemon.PokemonResponse;
import com.github.mangila.pokedex.shared.pokeapi.response.species.SpeciesResponse;
import com.github.mangila.pokedex.shared.repository.PokemonSpeciesRepository;
import com.github.mangila.pokedex.shared.repository.document.PokemonSpeciesDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class PokemonTask {

    private static final Logger logger = LoggerFactory.getLogger(PokemonTask.class);
    private final PokeApiTemplate pokeApiTemplate;
    private final PokemonSpeciesRepository pokemonSpeciesRepository;
    private final PokemonMediaHandler pokemonMediaHandler;

    public PokemonTask(PokeApiTemplate pokeApiTemplate, 
                      PokemonSpeciesRepository pokemonSpeciesRepository,
                      PokemonMediaHandler pokemonMediaHandler) {
        this.pokeApiTemplate = pokeApiTemplate;
        this.pokemonSpeciesRepository = pokemonSpeciesRepository;
        this.pokemonMediaHandler = pokemonMediaHandler;
    }

    /**
     * Processes a given PokemonEntry by retrieving relevant species, evolution chain, and varieties information
     * from the PokeApi, constructing a PokemonSpeciesDocument, saving it into the repository, and queuing related media
     * for processing.
     *
     * @param pokemonEntry the Pokemon entry containing the name and PokeApiUri used as the entry point for fetching species data
     */
    public void run(PokemonEntry pokemonEntry) {
        logger.info("Processing Pokemon entry: name={}, uri={}", pokemonEntry.name(), pokemonEntry.pokeApiUri());

        logger.debug("Fetching species data for: {}", pokemonEntry.name());
        var speciesResponse = pokeApiTemplate.fetchByUrl(pokemonEntry.pokeApiUri(), SpeciesResponse.class);

        logger.debug("Fetching evolution chain for species id: {}", speciesResponse.id());
        var evolutionChainResponse = pokeApiTemplate.fetchByUrl(PokeApiUri.create(speciesResponse.evolutionChain().url()), EvolutionChainResponse.class);

        logger.info("Processing {} varieties for Pokemon: {}", speciesResponse.varieties().size(), pokemonEntry.name());
        var varieties = speciesResponse.varieties()
                .stream()
                .peek(variety -> logger.debug("Processing variety: name={}, pokemon={}", variety.pokemon().name(), pokemonEntry.name()))
                .map(variety -> variety.pokemon().url())
                .map(PokeApiUri::create)
                .map(uri -> pokeApiTemplate.fetchByUrl(uri, PokemonResponse.class))
                .toList();

        logger.debug("Creating Pokemon species document from collected data");
        var document = PokemonSpeciesDocument.fromSpeciesEvolutionAndVarieties(speciesResponse, evolutionChainResponse, varieties);

        logger.info("Saving Pokemon species document: id={}, name={}", document.id(), document.names().get(0).name());
        pokemonSpeciesRepository.save(document);

        logger.debug("Queueing media for {} varieties", varieties.size());
        varieties.forEach(variety -> pokemonMediaHandler.queueMedia(Pair.of(speciesResponse.id(), variety.id()),
                variety.name(),
                variety.sprites(),
                variety.cries()));

        logger.info("Completed processing Pokemon entry: {}", pokemonEntry.name());
    }

}
