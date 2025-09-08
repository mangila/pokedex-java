# Pokedex App

The app module is the production entry point for the Pokedex application. It wires all other modules together and owns the runtime lifecycle.

What it does:
- Initializes named blocking queues used across modules.
- Configures singletons: PokemonDatabase and PokeApiClient with default settings.
- Starts the Scheduler and registers domain tasks.
- Blocks the main thread until a shutdown signal is enqueued.
- Registers a JVM shutdown hook to cleanup resources.

Key classes:
- com.github.mangila.pokedex.app.Application (main)
- com.github.mangila.pokedex.app.Bootstrap

Shutdown order:
1) PokeApiClient.shutdown()
2) Scheduler.shutdown()
3) PokemonDatabase.instance().close()
4) VirtualThreadFactory.terminateGracefully(...)