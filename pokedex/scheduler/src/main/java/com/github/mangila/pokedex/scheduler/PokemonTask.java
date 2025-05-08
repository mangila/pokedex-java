package com.github.mangila.pokedex.scheduler;

public class PokemonTask implements Runnable {

    private final QueueService queueService;

    public PokemonTask(QueueService queueService) {
        this.queueService = queueService;
    }

    @Override
    public void run() {
    }

}
