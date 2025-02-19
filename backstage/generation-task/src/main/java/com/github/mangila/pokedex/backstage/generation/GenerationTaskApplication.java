package com.github.mangila.pokedex.backstage.generation;

import com.github.mangila.pokedex.backstage.shared.integration.response.generation.GenerationResponse;
import com.github.mangila.pokedex.backstage.shared.model.Generation;
import com.github.mangila.pokedex.backstage.shared.model.Task;
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
@RegisterReflectionForBinding({GenerationResponse.class, Generation.class})
public class GenerationTaskApplication {
    Logger log = LoggerFactory.getLogger(GenerationTaskApplication.class);
    private final Task generationTask;

    public GenerationTaskApplication(Task generationTask) {
        this.generationTask = generationTask;
    }

    public static void main(String[] args) {
        SpringApplication.run(GenerationTaskApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> generationTask.run();
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
