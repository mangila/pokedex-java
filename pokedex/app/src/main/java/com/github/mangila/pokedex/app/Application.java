package com.github.mangila.pokedex.app;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Application {
    public static final BlockingQueue<Boolean> SHUTDOWN_QUEUE = new ArrayBlockingQueue<>(1);

    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.configurePokemonDatabase();
        bootstrap.configurePokeApiClient();
        bootstrap.initQueues();
        bootstrap.initScheduler();
        while (true) {
            Boolean shutdown = SHUTDOWN_QUEUE.take();
            if (shutdown) {
                break;
            }
        }
    }
}