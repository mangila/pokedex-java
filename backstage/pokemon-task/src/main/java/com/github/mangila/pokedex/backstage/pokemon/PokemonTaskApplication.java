package com.github.mangila.pokedex.backstage.pokemon;

import com.github.mangila.pokedex.backstage.pokemon.task.PokemonTask;
import com.github.mangila.pokedex.backstage.shared.integration.response.generation.GenerationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.github.mangila.pokedex.backstage"})
@RegisterReflectionForBinding({GenerationResponse.class})
public class PokemonTaskApplication {

    Logger log = LoggerFactory.getLogger(PokemonTaskApplication.class);
    private final PokemonTask pokemonTask;

    public PokemonTaskApplication(PokemonTask pokemonTask) {
        this.pokemonTask = pokemonTask;
    }

    public static void main(String[] args) {
        SpringApplication.run(PokemonTaskApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> pokemonTask.run();
    }

    /**
     * If an uncaught exception is thrown. Exit with a failure code.
     *
     * @return 1 - exitCode
     */
    @Bean
    public ExitCodeExceptionMapper exitCodeExceptionMapper() {
        return exception -> {
            log.error("ERR", exception);
            return 1;
        };
    }
}

