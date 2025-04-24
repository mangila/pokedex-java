package com.github.mangila.pokedex.scheduler;

import com.github.mangila.pokedex.scheduler.domain.MediaEntry;
import com.github.mangila.pokedex.scheduler.domain.PokemonEntry;
import com.github.mangila.pokedex.scheduler.service.MediaTask;
import com.github.mangila.pokedex.scheduler.service.PokemonTask;
import com.github.mangila.pokedex.scheduler.service.QueueService;
import com.github.mangila.pokedex.shared.model.PokeApiUri;
import com.github.mangila.pokedex.shared.repository.PokemonSpeciesRepository;
import com.github.mangila.pokedex.shared.repository.document.PokemonSpeciesDocument;
import com.github.mangila.pokedex.shared.repository.document.embedded.PokemonDocument;
import com.github.mangila.pokedex.shared.repository.document.embedded.PokemonMediaDocument;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "app.scheduler.enabled=false")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SchedulerApplicationTests {

    @Autowired
    private PokemonTask pokemonTask;
    @Autowired
    private MediaTask mediaTask;
    @Autowired
    private QueueService queueService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private PokemonSpeciesRepository pokemonSpeciesRepository;

    @Test
    @Order(1)
    void testPokemonTask() {
        queueService.add(QueueService.POKEMON_QUEUE, new PokemonEntry("ditto", PokeApiUri.create("https://pokeapi.co/api/v2/pokemon-species/ditto")));
        var poll = queueService.poll(QueueService.POKEMON_QUEUE, PokemonEntry.class);
        assertThat(poll).isNotEmpty();
        assertThatNoException().isThrownBy(() -> pokemonTask.run(poll.get()));
        var hasKeys = List.of(
                redisTemplate.hasKey("https://pokeapi.co/api/v2/pokemon-species/ditto"),
                redisTemplate.hasKey("https://pokeapi.co/api/v2/evolution-chain/66/"),
                redisTemplate.hasKey("https://pokeapi.co/api/v2/pokemon/132/")
        );
        assertThat(hasKeys).containsOnly(
                Boolean.TRUE,
                Boolean.TRUE,
                Boolean.TRUE);
        assertThat(pokemonSpeciesRepository.findById(132))
                .isNotEmpty()
                .map(PokemonSpeciesDocument::name)
                .get()
                .isEqualTo("ditto");
        assertThat(queueService.isEmpty(QueueService.POKEMON_QUEUE))
                .isTrue();
        assertThat(queueService.isEmpty(QueueService.MEDIA_QUEUE))
                .isFalse();
    }

    @Test
    @Order(2)
    void testPokemonTaskSadPath() {
        assertThatThrownBy(() -> pokemonTask.run(new PokemonEntry("POKEMON_NOT_EXIST", PokeApiUri.create("http://pokeapi.co"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should be 'https'");
    }

    @Test
    @Order(3)
    void testMediaTask() {
        assertThat(pokemonSpeciesRepository.findById(132))
                .isNotEmpty()
                .map(PokemonSpeciesDocument::varieties)
                .map(List::getFirst)
                .map(PokemonDocument::media)
                .contains(Collections.emptyList());
        var poll = queueService.poll(QueueService.MEDIA_QUEUE, MediaEntry.class);
        assertThat(poll).isNotEmpty();
        assertThatNoException().isThrownBy(() -> mediaTask.run(poll.get()));
        assertThat(pokemonSpeciesRepository.findById(132))
                .isNotEmpty()
                .map(PokemonSpeciesDocument::varieties)
                .map(List::getFirst)
                .map(PokemonDocument::media)
                .map(List::getFirst)
                .map(PokemonMediaDocument::fileName)
                .isNotEmpty();
    }
}
