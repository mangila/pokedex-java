# pokedex-spring-boot

Maven multi-module project consuming PokeAPI

## integration

Integration layer - fetches data over http to PokeApi

## model

All models shared around the project

## scheduler

Spring Scheduler that fetches data from PokeApi put it on a Queue,
polls and updates the database

## web

Graphql server 