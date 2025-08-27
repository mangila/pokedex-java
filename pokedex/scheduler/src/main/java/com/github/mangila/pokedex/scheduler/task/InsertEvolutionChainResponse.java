package com.github.mangila.pokedex.scheduler.task;

import com.github.mangila.pokedex.shared.queue.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record InsertEvolutionChainResponse(Queue queue) implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertEvolutionChainResponse.class);

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this,
                5,
                1,
                TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        queue.poll();
    }
}
