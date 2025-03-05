# pokedex-spring-boot

NOTE - this is still work in progress

Maven multi-module project consuming and displaying PokeAPI data.

* Redis for caching
* Mongodb as database
* Mongodb GridFS as media database
* Minikube for k8s deployment
* Native images for Tasks
* Native images for Bouncer applications

## Requirements

* GraalVM for playing around with the Native images in backstage module
* Minikube for playing around with the k8s deployment
* Golang SDK - to play around with image-converter in backstage module
* Protocol buffer compiler on host machine - to compile proto for Golang - The java maven has is it embedded

## api

Contains the file-api server and graphql server

## backstage

Tasks and middleware services that populates redis and mongodb with data
