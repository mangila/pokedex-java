# pokedex-spring-boot

Maven multi-module project consuming and displaying PokeAPI data.

* Redis for caching
* Mongodb as database
* Mongodb GridFS as media database
* Minikube for k8s deployment

## file-server

Spring Boot Web application - file-server to display or download images

* api/v1/file/{fileName}?download=true|false

## graphql-server

Spring Boot Web application, that serves the graphql endpoint

* /api/v1/graphql
* /graphiql

## integration

Integration layer - fetches data over http to PokeApi

## model

Models shared around the project

## repository

Database repository layer for MongoDb

## scheduler

Spring Scheduler that fetches data from PokeAPI and inserts to Redis and MongoDb

* Redis Set used a Queue
* Redis String used as key and value cache