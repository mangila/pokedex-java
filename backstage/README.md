# backstage

Background jobs compiled a Native Image for FaaS(Function as a Service)

## To run

Will compile everything and install and generate native executables for your host system

* `mvn -U clean install` - compile and install all modules
* `mvn native:compile -Pnative -DskipTests` - generate native executables - this might take a while

## Requirements

For local development

* GraalVM for compiling the native Image

## Docker

Dockerfiles for the tasks has a graalvm image ready to compile

* use "host.docker.internal" as host if running locally
* see minikube.ps1 for build step

# modules

## generation-task

* PokeApi request to fetch all pokemons from their Generation
* Puts all pokemons on PokemonQueue

## integration

Everything third party api

To use any integration `application.yml` must be configured with the server address

* Conditional bean for props to load the DI container

* PokeApi - RestClient
* Redis - Grpc Client
* MongoDb - Grpc Client

## media-task

* Polls from Media Queue
* Updates the pokemon document with the new media entry
* Adds reference to file server api for src (TODO: Service discovery stuffs or hardcoding)
* Puts Files into GridFS

## model

* everything structs
* Shared domain objects
* Shared Protos

## pokemon-task

* Fetch from PokeApi and checks already made http requests to Redis
* Updates Database with new pokemon
* Runs a side effect and puts Pokemon media (images and cries) to the Media Queue

## redis-bouncer

* Redis bouncer service to handle connection pooling
* Grpc Server
* Relays requests to and from Redis

## mongodb-bouncer

* Mongodb bouncer service to handle connection pooling
* Grpc Server
* Relays requests to and from MongoDb

