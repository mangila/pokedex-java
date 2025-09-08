# Pokedex Scheduler

The Scheduler module coordinates background tasks that fetch data from PokeAPI and insert it into the local database.

Responsibilities:
- Define Task abstractions and a TaskExecutor.
- Periodically run tasks that queue work and persist results.
- Provide lifecycle controls (init, shutdown).

Key classes:
- com.github.mangila.pokedex.scheduler.Scheduler
- com.github.mangila.pokedex.scheduler.SchedulerConfig
- com.github.mangila.pokedex.scheduler.task.Task, TaskExecutor
- Tasks: InsertCriesTask, InsertSpeciesResponseTask, InsertSpritesTask, InsertVarietyResponseTask, InsertEvolutionChainResponse, QueuePokemonsTask, ShutdownTask

How it works:
- Application bootstraps a ScheduledThreadPool (virtual threads) and registers the tasks.
- Scheduler.init() schedules each Task via TaskExecutor.
- Scheduler.shutdown() stops scheduling and shuts down the executor.