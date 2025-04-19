# pokedex-spring-boot

A Spring Boot application that serves as a Pokémon data aggregator and API, fetching data from PokéAPI and providing it
through GraphQL endpoints.

## Features

- Automated Pokemon data collection from PokéAPI
- Media file handling (images and sounds)
- GraphQL API for querying Pokemon data
- Distributed task processing using Redis
- File serving with proper cache control
- Support for various media types (PNG, JPG, OGG, GIF, SVG)

## Technologies

- Java 21
- Spring Boot 3.4.4
- MongoDB with GridFS for data and file storage
- Redis for task queue management
- GraphQL for API queries
- Project Reactor Netty for async operations
- TestContainers for integration testing
- Minikube for local Kubernetes development

## Prerequisites

- Java 21 or higher
- MongoDB
- Redis
- Maven
- Python 3.8+

## Building

### 1. Build Java Applications
