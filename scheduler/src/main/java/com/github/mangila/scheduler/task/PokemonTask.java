package com.github.mangila.scheduler.task;

import com.github.mangila.model.domain.Generation;
import com.github.mangila.model.domain.PokemonName;
import com.github.mangila.scheduler.queue.QueueService;
import com.github.mangila.scheduler.service.PokeApiService;
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
     * Fetches all Pokemons from all Generations and put it on Queue - on start up
     */
    @Override
    public void run(String... args) {
        EnumSet<Generation> enumSet = EnumSet.allOf(Generation.class);
        enumSet.forEach(generation -> {
            log.info("Fetching pokemons for {}", generation);
            var generations = pokeApiService.fetchPokemonSpecies(generation);
            generations
                    .pokemonSpecies()
                    .forEach(species -> queueService.add(new PokemonName(species.name())));
        });
    }


    /**
     * Polls from Queue and updates the database
     */
    @Scheduled(fixedRate = 1000)
    public void pollPokemon() {
        var pokemonSpecies = queueService.poll();
        if (Objects.isNull(pokemonSpecies)) {
            return;
        }
        log.info("Pokemon {}", "asdf");
    }
}
