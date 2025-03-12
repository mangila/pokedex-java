# pokedex-spring-boot

- NOTE - this is still work in progress
    - TODO - error handling for failed messages
    - TODO - file-api server discovery or static config
    - TODO - finish up k8s deployment

Maven multi-module project(and some Golang!) consuming and displaying PokeAPI data

* Datasource
    * Redis for caching
    * Mongodb as database
    * GridFs as media database or object storage
* Deployment
    * Containers
    * Minikube cluster

## Requirements

For local development

- GraalVM
    - native image compilation
- Go SDK
    - libwebp - https://developers.google.com/speed/webp/docs/api
- Protocol buffer compiler
    - Maven do the compilation automatically
    - Go protoc generator - `go install google.golang.org/protobuf/cmd/protoc-gen-go@latest`
    - Go protoc Grpc generator - `go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest`

## Quickstart

* Install Docker
    * run `docker compose -f docker-compose-db.yml up --force-recreate -d`
    * start the databases on localhost for k8s external access
* Install Minikube
    * `minikube start`
    * `minikube dashboard` - for some nice visualization
* run `docker-build-all.ps1` - this might take a while
* run `minikube-all.ps1`
* Should do the trick!

## api

Contains the file-api server and graphql server

## backstage

Tasks and middleware services that populates redis and mongodb with data
