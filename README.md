# Pokedex Spring Boot

A modern Pokedex application built with Spring Boot, GraphQL, MongoDB, and Redis. This project provides a GraphQL API
for querying Pokemon data and a scheduler for fetching and updating Pokemon data from the PokeAPI.

#### Requirements

Why build this project?

- Discover how to create a simple casual application and see how much pain there is to build it using these
  technologies and ecosystem.

Spring Boot and Java with its ecosystem to fulfill the requirements:

- [x] HTTP request to third-party API
    - [x] GZIP compression
- [x] Daemon Service to fetch and transform data
    - [x] CronJob or anything else to run periodically, some kind of trigger
- [x] Expose endpoints to serve data
    - [x] REST, GraphQL, etc.
- [x] Media processing
    - [?] Convert images to .webp
- [x] Media storage
    - [x] Client for GridFS, S3, AZ Blob Storage, etc.
- [x] Data caching
    - [x] Client for Redis, Memcached, etc.
- [x] Data storage
    - [x] Client for MongoDB, MySQL, etc.
- [WIP] Integration and unit testing
- [WIP] Real-time data processing
    - [WIP] Websocket, SSE, etc.
- [x] Deployment
    - [x] Build artifact and run

<img src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png" alt="pikachu" width="200"/>

## Features

- **GraphQL API**: Query Pokemon data using GraphQL
- **Automatic Data Synchronization**: Scheduler fetches and updates Pokemon data from the PokeAPI
- **MongoDB Storage**: Pokemon data is stored in MongoDB
- **Redis Caching**: Frequently accessed data is cached in Redis
- **Kubernetes Ready**: Deployment configurations for Kubernetes included
- **Docker Support**: Containerized for easy deployment

## Architecture

The project is structured as a multi-module Maven project:

- **graphql**: Provides the GraphQL API for querying Pokemon data
- **scheduler**: Fetches and updates Pokemon data from the PokeAPI
- **shared**: Common code shared between the graphql and scheduler modules

### Data Flow

1. The scheduler module fetches Pokemon data from the PokeAPI
2. The data is processed and stored in MongoDB
3. The graphql module provides a GraphQL API for querying the data
4. Frequently accessed data is cached in Redis for improved performance

## Technologies

- **Java 21**: Latest Java version for modern language features
- **Spring Boot 3.4.4**: Framework for building Java applications
- **Spring Data MongoDB**: MongoDB integration for Spring
- **Spring Data Redis**: Redis integration for Spring
- **Spring GraphQL**: GraphQL integration for Spring
- **MongoDB**: NoSQL database for storing Pokemon data
- **Redis**: In-memory data store for caching
- **Kubernetes**: Container orchestration for deployment
- **Docker**: Containerization for easy deployment
- **Maven**: Build and dependency management
- **Lombok**: Reduces boilerplate code
- **TestContainers**: Integration testing with real containers

## Getting Started

### Prerequisites

- Minikube (for local Kubernetes deployment)
- Java 21 or later
- Python 3.8 or later
- Docker

### Deployment with Minikube

The easiest way to deploy the application is by using the provided minikube.py script:

1. Start Minikube and start the MongoDB and Redis containers:
   ```bash
   docker compose -f docker-compose-db.yml up -d
   minikube start
   ```

2. Run the deployment script:
   ```bash
   python minikube.py
   ```

   This script will:

    - Check minikube status
    - create access to external MongoDB and Redis to your local Kubernetes cluster
    - Build and package the application modules
    - Build Docker images for each service
    - Deploy all services to your minikube cluster

3. Access the GraphQL API:
   ```bash
   minikube service pokedex-graphql-service -n pokedex
   ```

## API Usage

### Datasources

When running the application locally, the following datasources are available:

- Redis Cache—http://localhost:6379
- Redis Insight—http://localhost:8001
- Mongodb Connection String - `mongodb://admin:password@localhost:27017`

### GraphQL API

The GraphQL API is available at `/graphql`. You can use tools like GraphiQL or Postman to interact with it.

#### Example Queries

Find a Pokemon by ID:

```graphql
query {
    findById(id: 25) {
        name
        generation
        names {
            name
            language
        }
        varieties {
            name
            types {
                type
            }
            stats {
                name
                value
            }
            media {
                file_name
            }
        }
    }
}
```

Find a Pokemon by name:

```graphql
query {
    findByName(name: "pikachu") {
        name
        generation
        names {
            name
            language
        }
        varieties {
            name
            types {
                type
            }
            stats {
                name
                value
            }
            media {
                file_name
            }
        }
    }
}
```

## License

This project is licensed under the MIT License—see the LICENSE file for details.

## Acknowledgements

- [PokeAPI](https://pokeapi.co/) for providing the Pokemon data
- [Spring Boot](https://spring.io/projects/spring-boot) for the application framework
- [GraphQL](https://graphql.org/) for the query language
- [MongoDB](https://www.mongodb.com/) for the database
- [Redis](https://redis.io/) for caching
- [Kubernetes](https://kubernetes.io/) for container orchestration
