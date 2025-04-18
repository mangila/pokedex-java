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

## Prerequisites

- Java 21 or higher
- MongoDB
- Redis
- Maven

## Building

## Architecture

The application consists of several key components:

1. **Scheduler**: Coordinates the data processing pipeline
2. **Task Processors**:
    - Pokemon Task: Handles Pokemon data processing
    - Media Task: Manages media file downloads and storage
3. **API Layer**:
    - GraphQL endpoints for data queries
    - REST endpoints for media file serving

## Data Flow

1. Initial data fetch from PokéAPI
2. Task queuing in Redis
3. Scheduled processing of queued tasks
4. Storage in MongoDB/GridFS
5. Data serving through GraphQL/REST APIs