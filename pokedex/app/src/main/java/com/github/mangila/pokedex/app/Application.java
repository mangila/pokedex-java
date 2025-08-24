package com.github.mangila.pokedex.app;

import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.scheduler.Scheduler;
import com.github.mangila.pokedex.shared.util.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.mangila.pokedex.shared.Config.DELETE_DATABASE;
import static com.github.mangila.pokedex.shared.Config.TRUNCATE_DATABASE;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final AtomicBoolean APPLICATION_RUNNING = new AtomicBoolean(Boolean.FALSE);
    private static final ExecutorService SCHEDULER_RUNNER = VirtualThreadFactory.newSingleThreadExecutor();

    public static void main(String[] args) {
        APPLICATION_RUNNING.set(Boolean.TRUE);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.configurePokemonDatabase();
        bootstrap.configurePokeApiClient();
        bootstrap.initQueues();
        Scheduler scheduler = bootstrap.initScheduler();
        SCHEDULER_RUNNER.submit(() -> {
            while (Scheduler.RUNNING.get()) {
                if (Scheduler.SHUTDOWN.get()) {
                    scheduler.shutdownAllTasks();
                    break;
                }
            }
        });
        while (APPLICATION_RUNNING.get()) {
            if (!Scheduler.RUNNING.get() && !SCHEDULER_RUNNER.isTerminated()) {
                LOGGER.info("Shutting down SCHEDULER_RUNNER");
                VirtualThreadFactory.terminateGracefully(SCHEDULER_RUNNER, Duration.ofSeconds(30));
                APPLICATION_RUNNING.set(Boolean.FALSE);
            }
        }
        PokemonDatabase db = PokemonDatabase.getInstance();
    }
}