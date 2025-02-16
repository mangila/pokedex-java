package com.github.mangila.scheduler.task;

import com.github.mangila.model.domain.Generation;
import com.github.mangila.model.domain.PokemonName;
import com.github.mangila.scheduler.service.PokeApiService;
import com.github.mangila.scheduler.service.QueueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.EnumSet;
import java.util.Objects;

@Component
@Slf4j
@AllArgsConstructor
@Validated
public class PokemonTask implements CommandLineRunner {

    private final QueueService queueService;
    private final PokeApiService pokeApiService;

    /**
     * Fetches all Pokemons from all Generations and put it on Queue(Redis List) - on start up
     */
    @Override
    public void run(String... args) {
        EnumSet<Generation> enumSet = EnumSet.allOf(Generation.class);
        enumSet.forEach(generation -> {
            log.info("Generation push to Queue: {}", generation.getName());
            pokeApiService.fetchGeneration(generation)
                    .pokemonSpecies()
                    .stream()
                    .map(species -> new PokemonName(species.name()))
                    .forEach(queueService::push);
        });
    }

    /**
     * Polls from Redis List Queue and updates the database
     */
    @Scheduled(fixedRate = 1000)
    public void pollPokemon() {
        var pokemonName = queueService.pop();
        if (Objects.isNull(pokemonName)) {
            return;
        }
        log.info("Processing - {}", pokemonName.getName());
        var speciesResponse = pokeApiService.fetchPokemonSpecies(pokemonName);
        var evolutionChain = pokeApiService.fetchEvolutionChain(speciesResponse.evolutionChain());
        var varieties = speciesResponse.varieties()
                .stream()
                .map(pokeApiService::fetchPokemon)
                .toList();
    }
}
