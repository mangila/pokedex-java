package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiClient;
import com.github.mangila.pokedex.api.client.pokeapi.PokeApiUri;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.scheduler.KeyUriPair;
import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

public class InsertCriesTask implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertCriesTask.class);
    private final PokeApiClient pokeApiClient;
    private final BlockingQueue queue;
    private final PokemonDatabase pokemonDatabase;

    public InsertCriesTask(PokeApiClient pokeApiClient, BlockingQueue queue, PokemonDatabase pokemonDatabase) {
        this.pokeApiClient = pokeApiClient;
        this.queue = queue;
        this.pokemonDatabase = pokemonDatabase;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule(ScheduledExecutorService executor) {
        executor.submit(this);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            QueueEntry queueEntry;
            try {
                queueEntry = queue.take();
            } catch (InterruptedException e) {
                LOGGER.info("{} interrupted", name());
                //   VirtualThreadFactory.terminateGracefully(workerPool);
                Thread.currentThread().interrupt();
                break;
            }
            KeyUriPair uri = queueEntry.unwrapAs(KeyUriPair.class);
        }
    }
}