# Pokedex Spring Boot

A modern Pokedex application built with Spring Boot, GraphQL, MongoDB, and Redis. This project provides a GraphQL API
for querying Pokemon data and a scheduler for fetching and updating Pokemon data from the PokeAPI.

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

- Java 21 or later
- Maven 3.6 or later
- Docker and Docker Compose (for local development)
- Minikube (for local Kubernetes deployment)
- Python 3.8 or later (for automated local Kubernetes deployment)

### Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/mangila/pokedex-spring-boot.git
   cd pokedex-spring-boot
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the applications:
   ```bash
   # Run the scheduler to fetch Pokemon data
   java -jar scheduler/target/scheduler.jar
   
   # Run the GraphQL API
   java -jar graphql/target/api.jar
   ```

### Docker Deployment

1. Build the Docker images:
   ```bash
   # Build the scheduler image
   docker build -t pokedex-scheduler:latest -f scheduler/Dockerfile .
   
   # Build the GraphQL API image
   docker build -t pokedex-graphql:latest -f graphql/Dockerfile .
   ```

2. Run the containers:
   ```bash
   docker-compose up -d
   ```

### Kubernetes Deployment

1. Start Minikube:
   ```bash
   minikube start
   ```

2. Deploy MongoDB:
   ```bash
   kubectl apply -f k8s-external-database.yml
   ```

3. Deploy the applications:
   ```bash
   # Deploy the scheduler
   kubectl apply -f scheduler/k8s.yml
   
   # Deploy the GraphQL API
   kubectl apply -f graphql/k8s.yml
   ```

4. Access the GraphQL API:
   ```bash
   minikube service pokedex-graphql-service -n pokedex
   ```
5. Run python script(Optional):
   ```bash
   python minikube.py
   ```

## API Usage

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

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements

- [PokeAPI](https://pokeapi.co/) for providing the Pokemon data
- [Spring Boot](https://spring.io/projects/spring-boot) for the application framework
- [GraphQL](https://graphql.org/) for the query language
- [MongoDB](https://www.mongodb.com/) for the database
- [Redis](https://redis.io/) for caching
- [Kubernetes](https://kubernetes.io/) for container orchestration