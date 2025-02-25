# Backstage

- Tasks that is compiled as Native Images used a Function as a Service(FaaS)
- Bouncer applications that relays and handle connection pooling to the datasources
    - Redis
    - Mongodb

## To Run

Native images are generated based on your OS

- `mvn -U clean install` - Compiles,tests and installs all modules.
- `mvn native:compile -Pnative` - Generates native executables. Note that this might take some time.
- `mvn -PnativeTest test` - Run native image tests

## Requirements

For local development:

- GraalVM for compiling the native image.

## Docker

Dockerfiles for the tasks and bouncers include a GraalVM image ready to compile.

- Refer to `minikube.ps1` for the build steps.

## Modules

### generation-task

- Makes a PokeApi request to fetch all Pokémon from their Generation.
- Add Stream logs to `pokemon-name-event`

### integration

Handles all third-party APIs:

- PokeApi - RestClient
- Redis - gRPC Client
- MongoDB - gRPC Client

### media-task

- Read Stream Logs from `pokemon-name-event`
- Updates the Pokémon document with the new media entry.
- Adds a reference to the file server API for the source (TODO: Service discovery setup or hardcoding).
- Inserts media into GridFS.

### model

- Contains all struct definitions.
- Shared domain objects.
- Shared Proto implementations.
- Native image reflection stuffs

### pokemon-task

- Fetches data from PokeApi and checks already made HTTP requests to Redis.
- Updates the database with new Pokémon.
- Runs a side effect and Stream Logs Pokémon media (images and cries) into the `pokemon-media-event`.

### redis-bouncer

- Redis bouncer service to handle connection pooling.
- gRPC Server.
- Relays requests to and from Redis.

### mongodb-bouncer

- MongoDB bouncer service to handle connection pooling.
- gRPC Server.
- Relays requests to and from MongoDB.
