package com.github.mangila.pokedex.shared;

public interface SimpleBackgroundThread extends Runnable {

    void schedule();

    void shutdown();
}
