package com.github.mangila.pokedex.scheduler;

public class MediaTask implements Runnable {

    private final QueueService queueService;

    public MediaTask(QueueService queueService) {
        this.queueService = queueService;
    }

    @Override
    public void run() {

    }
}
