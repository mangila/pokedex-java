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
import org.springframework.data.util.Pair;
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
    @Scheduled(fixedRate = 5, initialDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void pollPokemon() {
        var optionalPokemonName = queueService.popNameQueue();
        if (optionalPokemonName.isEmpty()) {
            return;
        }
        var speciesName = optionalPokemonName.get();
        log.info("Processing - {}", speciesName.getName());
        var speciesResponse = pokeApiService.fetchPokemonSpecies(speciesName);
        var evolutionChain = pokeApiService.fetchEvolutionChain(speciesResponse.evolutionChain());
        var varieties = speciesResponse.varieties()
                .stream()
                .map(pokeApiService::fetchPokemon)
                .toList();
        // Run side effect
        varieties.forEach(variety -> {
            var idPair = Pair.of(
                    new PokemonId(speciesResponse.id()),
                    new PokemonId(variety.id()));
            var varietyName = new PokemonName(variety.name());
            putOnImagesQueue(idPair, varietyName, variety.sprites());
            putOnAudiosQueue(idPair, varietyName, variety.cries());
        });
        mongoDbService.save(pokeApiMapper.ToDocument(
                speciesResponse,
                evolutionChain,
                varieties
        ));
    }

    private void putOnAudiosQueue(Pair<PokemonId, PokemonId> idPair,
                                  PokemonName name,
                                  Cries cries) {
        queueAudioIfNotNull(idPair, name, "legacy", cries.legacy());
        queueAudioIfNotNull(idPair, name, "latest", cries.latest());
    }

    private void queueAudioIfNotNull(Pair<PokemonId, PokemonId> idPair,
                                     PokemonName name,
                                     String description,
                                     URL url) {
        if (Objects.nonNull(url)) {
            var media = new PokemonMedia(
                    idPair.getFirst(),
                    idPair.getSecond(),
                    name,
                    description,
                    url
            );
            queueService.pushAudioQueue(media);
        }
    }


    private void putOnImagesQueue(Pair<PokemonId, PokemonId> idPair,
                                  PokemonName name,
                                  Sprites sprites) {
        queueImageIfNotNull(idPair, name, "front-default", sprites.frontDefault());
        queueImageIfNotNull(idPair, name, "back-default", sprites.backDefault());
    }

    private void queueImageIfNotNull(Pair<PokemonId, PokemonId> idPair,
                                     PokemonName name,
                                     String description,
                                     URL url) {
        if (Objects.nonNull(url)) {
            var media = new PokemonMedia(
                    idPair.getFirst(),
                    idPair.getSecond(),
                    name,
                    description,
                    url
            );
            queueService.pushImageQueue(media);
        }
    }
}
