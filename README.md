# pokedex-spring-boot

Maven multi-module project consuming and displaying PokeAPI data.

* Redis for caching
* Mongodb as database
* Mongodb GridFS as media database
* Minikube for k8s deployment
* Native images for Tasks

## Requirements

* GraalVM for playing around with the Native images in backstage module

## api

Contains the file-api server and graphql server

## backstage

Tasks that populates redis and mongodb with data
