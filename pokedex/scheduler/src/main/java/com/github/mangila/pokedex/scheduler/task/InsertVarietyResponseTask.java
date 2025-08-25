package com.github.mangila.pokedex.scheduler.task;

public record InsertVarietyResponseTask() implements Task {

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void schedule() {

    }

    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public void run() {

    }
}
