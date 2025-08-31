package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.api.client.pokeapi.PokeApiUri;
import com.github.mangila.pokedex.api.db.PokemonDatabase;
import com.github.mangila.pokedex.shared.Pair;
import com.github.mangila.pokedex.shared.queue.BlockingQueue;
import com.github.mangila.pokedex.shared.queue.QueueEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

public class InsertVarietyResponseTask implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertVarietyResponseTask.class);
    private final BlockingQueue queue;
    private final PokemonDatabase pokemonDatabase;

    public InsertVarietyResponseTask(BlockingQueue queue, PokemonDatabase pokemonDatabase) {
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

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            QueueEntry queueEntry;
            try {
                queueEntry = queue.take();
            } catch (InterruptedException e) {
                LOGGER.info("{} interrupted", name());
                Thread.currentThread().interrupt();
                break;
            }
            Pair<String, PokeApiUri> uri = queueEntry.unwrapAs(Pair.class);
        }
    }
}