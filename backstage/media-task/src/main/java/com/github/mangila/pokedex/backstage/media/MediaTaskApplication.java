package com.github.mangila.pokedex.backstage.media;

import com.github.mangila.pokedex.backstage.media.props.MediaTaskProperties;
import com.github.mangila.pokedex.backstage.shared.model.func.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@SpringBootApplication
@ComponentScan({"com.github.mangila.pokedex.backstage"})
public class MediaTaskApplication {

    private static final Logger log = LoggerFactory.getLogger(MediaTaskApplication.class);

    private final MediaTaskProperties taskProperties;
    private final Task task;

    public MediaTaskApplication(MediaTaskProperties taskProperties,
                                Task mediaTask) {
        this.taskProperties = taskProperties;
        this.task = mediaTask;
    }

    public static void main(String[] args) {
        SpringApplication.run(MediaTaskApplication.class, args);
    }

    /**
     * Native Images cannot decide on a ConditionalBean - run condition on Runtime instead.
     */
    @Bean
    public CommandLineRunner commandLineRunner(Environment environment) {
        log.info("{}", taskProperties.getMediaStreamKey());
        var isTestProfile = environment.acceptsProfiles(Profiles.of("test"));
        if (isTestProfile) {
            return args -> log.info("Running test profile - will not start PokemonTask");
        }
        return args -> task.run();
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
