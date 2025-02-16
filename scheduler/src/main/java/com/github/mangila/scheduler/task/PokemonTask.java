package com.github.mangila.scheduler.task;

import com.github.mangila.integration.pokeapi.response.pokemon.Cries;
import com.github.mangila.integration.pokeapi.response.pokemon.Sprites;
import com.github.mangila.model.domain.Generation;
import com.github.mangila.model.domain.PokemonId;
import com.github.mangila.model.domain.PokemonMedia;
import com.github.mangila.model.domain.PokemonName;
import com.github.mangila.scheduler.mapper.PokeApiMapper;
import com.github.mangila.scheduler.service.MongoDbService;
import com.github.mangila.scheduler.service.PokeApiService;
import com.github.mangila.scheduler.service.QueueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@AllArgsConstructor
public class PokemonTask implements CommandLineRunner {

    private final QueueService queueService;
    private final MongoDbService mongoDbService;
    private final PokeApiService pokeApiService;
    private final PokeApiMapper pokeApiMapper;

    /**
     * Fetches all Pokemons from all Generations and put it on Queue(Redis Set) - on start up
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
                    .forEach(queueService::pushNameQueue);
        });
    }

    /**
     * Polls from Redis Set Queue and updates the database
     * Puts Sprites and Cries on Queue for a side effect
     */
    @Scheduled(fixedRate = 5, initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void pollPokemon() {
        var name = queueService.popNameQueue();
        if (Objects.isNull(name)) {
            return;
        }
        log.info("Processing - {}", name.getName());
        var speciesResponse = pokeApiService.fetchPokemonSpecies(name);
        var evolutionChain = pokeApiService.fetchEvolutionChain(speciesResponse.evolutionChain());
        var varieties = speciesResponse.varieties()
                .stream()
                .map(pokeApiService::fetchPokemon)
                .toList();
        // Run side effect
        varieties.forEach(variety -> {
            var id = new PokemonId(variety.id());
            putOnImagesQueue(id, name, variety.sprites());
            putOnAudiosQueue(id, name, variety.cries());
        });
        mongoDbService.save(pokeApiMapper.ToDocument(
                speciesResponse,
                evolutionChain,
                varieties
        ));
    }

    private void putOnAudiosQueue(PokemonId id, PokemonName name, Cries cries) {
        queueAudioIfNotNull(id, name, "legacy", cries.legacy());
        queueAudioIfNotNull(id, name, "latest", cries.latest());
    }

    private void queueAudioIfNotNull(PokemonId id,
                                     PokemonName name,
                                     String description,
                                     URL url) {
        if (Objects.nonNull(url)) {
            queueService.pushAudioQueue(new PokemonMedia(id, name, description, url));
        }
    }


    private void putOnImagesQueue(PokemonId id,
                                  PokemonName name,
                                  Sprites sprites) {
        queueImageIfNotNull(id, name, "front-default", sprites.frontDefault());
        queueImageIfNotNull(id, name, "back-default", sprites.backDefault());
    }

    private void queueImageIfNotNull(PokemonId id,
                                     PokemonName name,
                                     String description,
                                     URL url) {
        if (Objects.nonNull(url)) {
            queueService.pushImageQueue(new PokemonMedia(id, name, description, url));
        }
    }
}
