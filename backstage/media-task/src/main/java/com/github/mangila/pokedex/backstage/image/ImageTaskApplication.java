package com.github.mangila.pokedex.backstage.image;

import com.github.mangila.pokedex.backstage.shared.model.Task;
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
public class ImageTaskApplication {

    Logger log = LoggerFactory.getLogger(ImageTaskApplication.class);
    private final Task imageTask;

    public ImageTaskApplication(Task imageTask) {
        this.imageTask = imageTask;
    }

    public static void main(String[] args) {
        SpringApplication.run(ImageTaskApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> imageTask.run();
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
