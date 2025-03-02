package com.github.mangila.pokedex.backstage.pokemon;

import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.github.mangila.pokedex.backstage"})
public class PokemonTaskApplication {

    private static final Logger log = LoggerFactory.getLogger(PokemonTaskApplication.class);
    private final Task task;

    public PokemonTaskApplication(Task pokemonTask) {
        this.task = pokemonTask;
    }

    public static void main(String[] args) {
        SpringApplication.run(PokemonTaskApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return task::run;
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

