package com.github.mangila.pokedex.scheduler.service;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.scheduler.domain.PokemonEntry;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.pokeapi.PokeApiTemplate;
import com.github.mangila.pokedex.shared.pokeapi.response.allpokemons.AllPokemonsResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
@lombok.AllArgsConstructor
@lombok.extern.slf4j.Slf4j
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true")
public class Scheduler {

    private final PokemonTask pokemonTask;
    private final MediaTask mediaTask;
    private final PokeApiTemplate pokeApiTemplate;
    private final QueueService queueService;

    @PostConstruct
    public void queueAllPokemonSpecies() {
        var pokeApiUri = PokeApiUri.create("https://pokeapi.co/api/v2/pokemon-species/?&limit=1025");
        pokeApiTemplate.fetchByUrl(pokeApiUri, AllPokemonsResponse.class)
                .results()
                .stream()
                .map(PokemonEntry::fromResult)
                .forEach(entry -> queueService.add(QueueService.POKEMON_QUEUE, entry));
    }

    @Scheduled(initialDelay = 5, fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void pollPokemon() {
        var entryOptional = queueService.poll(QueueService.POKEMON_QUEUE, PokemonEntry.class);
        if (entryOptional.isPresent()) {
            try {
                pokemonTask.run(entryOptional.get());
            } catch (Exception e) {
                log.error("Failed to process entry", e);
                queueService.add(QueueService.POKEMON_QUEUE, entryOptional.get());
            }
        }
    }


    @Scheduled(initialDelay = 60, fixedRate = 3, timeUnit = TimeUnit.SECONDS)
    public void pollMedia() {
        var entryOptional = queueService.poll(QueueService.MEDIA_QUEUE, MediaEntry.class);
        if (entryOptional.isPresent()) {
            try {
                mediaTask.run(entryOptional.get());
            } catch (Exception e) {
                log.error("Failed to process entry", e);
                queueService.add(QueueService.MEDIA_QUEUE, entryOptional.get());
            }
        }
    }

    @Scheduled(initialDelay = 3, fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void shutdown() {
        if (queueService.isEmpty(QueueService.POKEMON_QUEUE) &&
                queueService.isEmpty(QueueService.MEDIA_QUEUE)) {
            System.exit(0);
        }
    }
}
