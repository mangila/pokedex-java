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

## cache

* everything redis

## db

* everything mongodb

## generation-task

* fetches the all the pokemons according to their Generation

## integration

* everything third party api - PokeApi

## media-task

* poll for queue and fetches and download the pokemon image from the Queue - updates database

## model

* everything structs - shared domain objects

## pokemon-task

* fetch Pokemon - update database - put images on Media queue

