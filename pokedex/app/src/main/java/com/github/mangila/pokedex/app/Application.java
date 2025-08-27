package com.github.mangila.pokedex.app;

public class Application {
    public static volatile boolean running = false;

    public static void main(String[] args) {
        running = true;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.configurePokemonDatabase();
        bootstrap.configurePokeApiClient();
        bootstrap.initQueues();
        bootstrap.initScheduler();
        while (running) {
            Thread.onSpinWait();
        }
    }
}